/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.matheus.fatguys.htm.jankenpo.gui;

import br.com.matheus.fatguys.htm.jankenpo.JanKenPoEnum;
import br.com.matheus.fatguys.htm.persistence.FileDao;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.numenta.nupic.network.Inference;
import org.numenta.nupic.network.sensor.Publisher;
import rx.Subscriber;

/**
 *
 * @author y2gh
 */
public class JanKenPoModel implements PropertyChangeListener {

    private List<JanKenPoEnum> possibleValues;
    private List<String> htmLogList = new ArrayList<>();
    private JanKenPoEnum htmGuess;
    private JanKenPoEnum randomGuess;
    private JanKenPoEnum yourGuess;
    private JanKenPoEnum htmPreviousGuess;
    private JanKenPoEnum randomPreviousGuess;
    private JanKenPoEnum yourPreviousGuess;
    private int yourPoints;
    private int htmPoints;
    private int randomPoints;
    private int yourPointsVsHtm;
    private final Publisher publisher = null;
    private FileDao fileDao;
    private JanKenPoGuiNetwork net;
    private String netName = "analysis-jokenpo-GUI-ONLINE.csv";
    private boolean versusHtm = true;
    private String playerName = "PLAYER1";
    private String prediction;
    private int anomaly=0;

    public JanKenPoModel() {
        this.possibleValues = Arrays.asList(JanKenPoEnum.values());
    }

    public void init() throws IOException {
        randomPredict();
        this.net = new JanKenPoGuiNetwork(netName, getSubscriber());

        if (net.isWasLoaded()) {
            logHtm("WELLCOME BACK, "+playerName+"!");
            FileDao fileLastPrediction = new FileDao(null, System.getProperty("user.home").concat(File.separator).concat(netName + ".last"));
            prediction = randomGuess + "";
            try {
                prediction = fileLastPrediction.nextLine();
                htmGuess = JanKenPoEnum.getFromPattern(prediction).winner();
                fileLastPrediction.closeFiles();
            } catch (IOException ex) {
                Logger.getLogger(JanKenPoModel.class.getName()).log(Level.SEVERE, null, ex);
            }
            logHtm("------------------------------------------------------------------------------> you'll choose " + prediction + " next time");
        } else {
            logHtm("YOU'RE BRAND NEW TO ME!");
            logHtm("------------------------------------------------------------------------------> So I'll randomly choose any option our first time");
        }

        this.fileDao = new FileDao(System.getProperty("user.home").concat(File.separator).concat(netName + ".csv"), null);

        String linha = "REC. NUM.;ANOM;PLAYER1;PLAYER2;GUESS;MATCH PREVIOUS;MATCH PREVIOUS COUNT;PLAYER 1 NEXT CHOICE";
        fileDao.persistLine(linha);

        net.runNetwork();
        System.out.println("We're ready!");
    }

    void reset() throws IOException {
        setHtmPreviousGuess(null);
        setYourPoints(0);
        setYourPointsVsHtm(0);
        setHtmPoints(0);
        setRandomPoints(0);
        init();
    }

    Subscriber<Inference> getSubscriber() {

        return new Subscriber<Inference>() {
            @Override
            public void onCompleted() {
                System.out.println("\nstream closed. See output file : " + fileDao.getOutputFileName());
                try {
                    fileDao.closeFiles();
                    FileDao fileLastPrediction = new FileDao(System.getProperty("user.home").concat(File.separator).concat(netName + ".last"), null);
                    fileLastPrediction.persistLine(prediction);
                    fileLastPrediction.closeFiles();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(Inference i) {

                Object p = i.getClassification("PLAYER1").getMostProbableValue(1);

                if (p != null) {
                    prediction = (String) p;
                    if (htmPreviousGuess == null) {
                        setHtmPreviousGuess(JanKenPoEnum.getFromPattern(prediction));
                    }
                } else {
                    prediction = "" + htmPreviousGuess;
                    if (htmPreviousGuess == null) {
                        if (net.isWasLoaded()) {
                            FileDao fileLastPrediction = new FileDao(null, System.getProperty("user.home").concat(File.separator).concat(netName + ".last"));
                            String lastPrediction = randomGuess + "";
                            try {
                                lastPrediction = fileLastPrediction.nextLine();
                            } catch (IOException ex) {
                                Logger.getLogger(JanKenPoModel.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            setHtmPreviousGuess(JanKenPoEnum.getFromPattern(lastPrediction).winner());
                        } else {
                            setHtmPreviousGuess(randomGuess);
                        }
                    }
                }

                String player1ActualValue = (String) i.getClassifierInput().get("PLAYER1").get("inputValue");
                String player2ActualValue = (String) i.getClassifierInput().get("PLAYER2").get("inputValue");
                Double anomalyScore = i.getAnomalyScore();
                setAnomaly ((int)(100 * anomalyScore));
                int recordNumber = i.getRecordNum();

                Boolean guessedCorrectly = JanKenPoEnum.getFromPattern(player1ActualValue).winner().equals(htmPreviousGuess);
                if (htmGuess != null) {
                    setHtmPreviousGuess(htmGuess);
                }
                setHtmGuess(JanKenPoEnum.getFromPattern(prediction).winner());

                //
                if (htmPreviousGuess == null) {
                    htmPreviousGuess = randomGuess;
                }
                if (yourGuess.wins(htmPreviousGuess)) {
                    setYourPointsVsHtm(yourPointsVsHtm + 1);
                } else if (htmPreviousGuess.wins(yourGuess)) {
                    setHtmPoints(htmPoints + 1);
                }

                String log = "YOU=" + yourGuess + "(" + yourPointsVsHtm + "), HTM=" + htmPreviousGuess + "(" + htmPoints + ")";
                if (prediction != null) {
                    log += "----------> you'll choose " + prediction + " next time";
                }
                logHtm(log);
                //                

                String linha = recordNumber + ";" + anomalyScore + ";" + player1ActualValue + ";" + player2ActualValue + ";" + htmGuess + ";" + guessedCorrectly + ";" + htmPoints + "; you'll choose " + prediction + " next time";
                fileDao.persistLine(linha);
            }
        };
    }

    public void randomPredict() {
        setRandomGuess(JanKenPoEnum.getFromId(new Random().nextInt(3)));
    }

    private void htmPredict() {
        String linha = yourGuess + "," + htmPreviousGuess;
        net.getPublisher().onNext(linha);
    }

    public void play() {
        if (randomGuess == null) {
            randomPredict();
        }

        if (htmGuess == null) {
            randomPredict();
            setHtmPreviousGuess(randomGuess);
        } else {
            setHtmPreviousGuess(htmGuess);
        }

        setRandomPreviousGuess(randomGuess);

        if (yourGuess.wins(randomPreviousGuess)) {
            setYourPoints(yourPoints + 1);
        } else if (randomPreviousGuess.wins(yourGuess)) {
            setRandomPoints(randomPoints + 1);
        }

        randomPredict();
        htmPredict();
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        Object old = this.playerName;
        this.playerName = playerName;
        firePropertyChange("playerName", old, playerName);
        netName = "analysis-jokenpo-GUI-" + playerName + "-ONLINE.htm";
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public JanKenPoEnum getHtmPreviousGuess() {
        return htmPreviousGuess;
    }

    public int getAnomaly() {
        return anomaly;
    }

    public void setAnomaly(int anomaly) {
        Object old = this.anomaly;
        this.anomaly = anomaly;
        firePropertyChange("anomaly", old, anomaly);
    }

    public void setHtmPreviousGuess(JanKenPoEnum htmPreviousGuess) {
        Object old = this.htmPreviousGuess;
        this.htmPreviousGuess = htmPreviousGuess;
        firePropertyChange("htmPreviousGuess", old, htmPreviousGuess);
    }

    public List<String> getHtmLogList() {
        return htmLogList;
    }

    public void setHtmLogList(List<String> htmLogList) {
        Object old = this.htmLogList;
        this.htmLogList = htmLogList;
        firePropertyChange("htmLogList", old, htmLogList);
    }

    public boolean isVersusHtm() {
        return versusHtm;
    }

    public void setVersusHtm(boolean versusHtm) {
        Object old = this.versusHtm;
        this.versusHtm = versusHtm;
        firePropertyChange("versusHtm", old, versusHtm);
    }

    public JanKenPoEnum getRandomPreviousGuess() {
        return randomPreviousGuess;
    }

    public void setRandomPreviousGuess(JanKenPoEnum randomPreviousGuess) {
        Object old = this.randomPreviousGuess;
        this.randomPreviousGuess = randomPreviousGuess;
        firePropertyChange("randomPreviousGuess", old, randomPreviousGuess);
    }

    public JanKenPoEnum getYourPreviousGuess() {
        return yourPreviousGuess;
    }

    public void setYourPreviousGuess(JanKenPoEnum yourPreviousGuess) {
        Object old = this.yourPreviousGuess;
        this.yourPreviousGuess = yourPreviousGuess;
        firePropertyChange("yourPreviousGuess", old, yourPreviousGuess);
    }

    public JanKenPoEnum getYourGuess() {
        return yourGuess;
    }

    public void setYourGuess(JanKenPoEnum yourGuess) {
        JanKenPoEnum old = this.yourGuess;
        setYourPreviousGuess(old);
        this.yourGuess = yourGuess;
        firePropertyChange("yourGuess", old, yourGuess);
    }

    public List<JanKenPoEnum> getPossibleValues() {
        return possibleValues;
    }

    public void setPossibleValues(List<JanKenPoEnum> possibleValues) {
        Object old = this.possibleValues;
        this.possibleValues = possibleValues;
        firePropertyChange("possibleValues", old, possibleValues);
    }

    public JanKenPoEnum getHtmGuess() {
        return htmGuess;
    }

    public void setHtmGuess(JanKenPoEnum htmGuess) {
        JanKenPoEnum old = this.htmGuess;
//        setHtmPreviousGuess(old);
        this.htmGuess = htmGuess;
        firePropertyChange("htmGuess", old, htmGuess);
    }

    public JanKenPoEnum getRandomGuess() {
        return randomGuess;
    }

    public void setRandomGuess(JanKenPoEnum randomGuess) {
        JanKenPoEnum old = this.randomGuess;
//        setRandomPreviousGuess(old);
        this.randomGuess = randomGuess;
        firePropertyChange("randomGuess", old, randomGuess);
    }

    public int getYourPoints() {
        return yourPoints;
    }

    public void setYourPoints(int yourPoints) {
        Object old = this.yourPoints;
        this.yourPoints = yourPoints;
        firePropertyChange("yourPoints", old, yourPoints);
    }

    public int getYourPointsVsHtm() {
        return yourPointsVsHtm;
    }

    public void setYourPointsVsHtm(int yourPointsVsHtm) {
        Object old = this.yourPointsVsHtm;
        this.yourPointsVsHtm = yourPointsVsHtm;
        firePropertyChange("yourPointsVsHtm", old, yourPointsVsHtm);
    }

    public int getHtmPoints() {
        return htmPoints;
    }

    public void setHtmPoints(int htmPoints) {
        Object old = this.htmPoints;
        this.htmPoints = htmPoints;
        firePropertyChange("htmPoints", old, htmPoints);
    }

    public int getRandomPoints() {
        return randomPoints;
    }

    public void setRandomPoints(int randomPoints) {
        Object old = this.randomPoints;
        this.randomPoints = randomPoints;
        firePropertyChange("randomPoints", old, randomPoints);
    }

    protected PropertyChangeSupport support = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void removeAllPropertyChangeListeners() {
        for (PropertyChangeListener propertyChangeListener : support.getPropertyChangeListeners()) {
            support.removePropertyChangeListener(propertyChangeListener);
        }
    }

    public void firePropertyChange(final String prop, final Object oldValue, final Object newValue) {
        support.firePropertyChange(prop, oldValue, newValue);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void logHtm(String linha) {
        List<String> log = new ArrayList<>();
        log.addAll(htmLogList);
        setHtmLogList(null);
        log.add(linha);
        setHtmLogList(log);
    }

    void saveNetwork() {
        this.net.saveNetwork();
    }

}
