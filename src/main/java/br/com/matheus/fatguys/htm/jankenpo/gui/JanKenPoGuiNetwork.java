/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.matheus.fatguys.htm.jankenpo.gui;

import java.io.IOException;
import org.numenta.nupic.Parameters;
import org.numenta.nupic.algorithms.Anomaly;
import org.numenta.nupic.algorithms.SpatialPooler;
import org.numenta.nupic.algorithms.TemporalMemory;
import org.numenta.nupic.network.Inference;
import org.numenta.nupic.network.Layer;
import org.numenta.nupic.network.Network;
import org.numenta.nupic.network.Persistence;
import org.numenta.nupic.network.PublisherSupplier;
import org.numenta.nupic.network.Region;
import org.numenta.nupic.network.sensor.ObservableSensor;
import org.numenta.nupic.network.sensor.Publisher;
import org.numenta.nupic.network.sensor.Sensor;
import org.numenta.nupic.network.sensor.SensorParams;
import org.numenta.nupic.network.sensor.SensorParams.Keys;
import org.numenta.nupic.serialize.SerialConfig;
import rx.Observer;
import rx.Subscriber;

/**
 *
 * @author y2gh
 */
public class JanKenPoGuiNetwork {
    
    private Network network;
    private Publisher publisher = null;
    private final Sensor sensor;
    private boolean wasLoaded=false;

    public JanKenPoGuiNetwork(String fileName, Subscriber<Inference> subscriber) throws IOException {
        
        PublisherSupplier supplier = PublisherSupplier.builder()
                .addHeader("PLAYER1,PLAYER2")
                .addHeader("string,string")
                .addHeader("T,") //see SensorFlags.java for more info                
                .build();

        this.sensor = Sensor.create(ObservableSensor::create, SensorParams.create(
                Keys::obs, new Object[]{"", supplier}));


        try {
            this.network = loadNetwork(fileName);
            this.wasLoaded=true;
            try {
                this.publisher = this.network.getPublisher();
            } catch (Exception e) {
                supplier.setNetwork(this.network);
                this.publisher = supplier.get();
            }

        } catch (IOException ex) {
            this.network = createBasicNetwork(fileName);
            supplier.setNetwork(this.network);
            //Get the Publisher from the PublisherSupplier - we'll use this to feed in data just as before
            this.publisher = supplier.get();
        }

        this.network.observe().subscribe(subscriber);

    }

    boolean isWasLoaded() {
        return wasLoaded;
    }

    public Publisher getPublisher() {
        return publisher;
    }
    

    /**
     * Creates a basic {@link Network} with 1 {@link Region} and 1
     * {@link Layer}. However this basic network contains all algorithmic
     * components.
     *
     * @param networkName
     * @return a basic Network
     */
    public Network createBasicNetwork(String networkName) {
        Parameters p = JanKenPoGuiNetworkSetup.getParameters();
        p = p.union(JanKenPoGuiNetworkSetup.getEncoderParams("PLAYER1"));
        

        // This is how easy it is to create a full running Network!
        return Network.create(networkName, p)
                .add(Network.createRegion("Region 1")
                        .add(Network.createLayer("Layer 2/3", p)
                                .alterParameter(Parameters.KEY.AUTO_CLASSIFY, Boolean.TRUE)
                                .add(Anomaly.create())
                                .add(new TemporalMemory())
                                .add(new SpatialPooler())
                                .add(sensor)));
    }

    /**
     * Simple run hook
     */
    public void runNetwork() throws IOException {
        network.start();
    }
    
    public void saveNetwork() {
        SerialConfig config = new SerialConfig(network.getName());
        Persistence.get(config).store(network);
    }

    public void checkPoint() {
        SerialConfig config = new SerialConfig(network.getName());
        Persistence.get(config).checkPointer(network).checkPoint(new Observer<byte[]>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable thrwbl) {
            }

            @Override
            public void onNext(byte[] t) {
            }
        });
    }

    public static Network loadNetwork(String networkName) throws IOException {
        SerialConfig config = new SerialConfig(networkName);
        return Persistence.get(config).load(networkName);
    }
    
}
