/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.matheus.fatguys.htm.jankenpo.gui;

import br.com.matheus.fatguys.htm.jankenpo.JanKenPoEnum;
import br.com.matheus.fatguys.htm.net.PredictionsUtil;
import java.util.HashMap;
import java.util.Map;
import org.numenta.nupic.Parameters;
import org.numenta.nupic.algorithms.Classifier;
import org.numenta.nupic.algorithms.SDRClassifier;

/**
 *
 * @author y2gh
 */
public class JanKenPoGuiNetworkSetup {
    
    
    /**
     * Sets up an Encoder Mapping of configurable values.
     *  
     * @param map               if called more than once to set up encoders for more
     *                          than one field, this should be the map itself returned
     *                          from the first call to {@code #setupMap(Map, int, int, double, 
     *                          double, double, double, Boolean, Boolean, Boolean, String, String, String)}
     * @param n                 the total number of bits in the output encoding     
     * @return
     */
    public static Map<String, Map<String, Object>> setupMap(
            Map<String, Map<String, Object>> map,
            int n, int w, String fieldName) {

        if(map == null) {
            map = new HashMap<String, Map<String, Object>>();
        }
        
        Map<String, Object> inner = null;
        if((inner = map.get(fieldName)) == null) {
            map.put(fieldName, inner = new HashMap<String, Object>());
        }

        inner.put("n", n);
        inner.put("w", w);
        inner.put("forced", true);
        inner.put("fieldType", "JoKenPo");
        inner.put("encoderType", "CategoryEncoder");
        inner.put("categoryList", JanKenPoEnum.valuesAsStringList());

        if(fieldName != null) inner.put("fieldName", fieldName);
        
        return map;
    }

    
    
    public static Map<String, Map<String, Object>> getFieldSEncodingMap() {
        Map<String, Map<String, Object>> fieldEncodings = setupMap(
                null,
                21, // n                
                7, // w                
                "PLAYER1");
        

        fieldEncodings = setupMap(
                fieldEncodings, 
                21,
                7,
                "PLAYER2");
        
        return fieldEncodings;
    }
    
    
    
    
    public static Parameters getEncoderParams(String campoInferencia) {
        Map<String, Map<String, Object>> fieldEncodings = getFieldSEncodingMap();

        Parameters p = Parameters.getEncoderDefaultParameters();
        p.set(Parameters.KEY.GLOBAL_INHIBITION, true);
        p.set(Parameters.KEY.COLUMN_DIMENSIONS, new int[] { 2048 });
        p.set(Parameters.KEY.CELLS_PER_COLUMN, 32);
        p.set(Parameters.KEY.NUM_ACTIVE_COLUMNS_PER_INH_AREA, 40.0);
        p.set(Parameters.KEY.POTENTIAL_PCT, 0.8);
        p.set(Parameters.KEY.SYN_PERM_CONNECTED,0.1);
        p.set(Parameters.KEY.SYN_PERM_ACTIVE_INC, 0.0001);
        p.set(Parameters.KEY.SYN_PERM_INACTIVE_DEC, 0.0005);
        p.set(Parameters.KEY.MAX_BOOST, 1.0);
        p.set(Parameters.KEY.INFERRED_FIELDS, getInferredFieldsMap("PLAYER1", SDRClassifier.class));
        PredictionsUtil.setSteps(new int[]{1});
        p.set(Parameters.KEY.INFERRED_STEPS, PredictionsUtil.getSteps());
        
        p.set(Parameters.KEY.MAX_NEW_SYNAPSE_COUNT, 20);
        p.set(Parameters.KEY.INITIAL_PERMANENCE, 0.21);
        p.set(Parameters.KEY.PERMANENCE_INCREMENT, 0.1);
        p.set(Parameters.KEY.PERMANENCE_DECREMENT, 0.1);
        p.set(Parameters.KEY.MIN_THRESHOLD, 9);
        p.set(Parameters.KEY.ACTIVATION_THRESHOLD, 12);
        
        p.set(Parameters.KEY.CLIP_INPUT, true);
        p.set(Parameters.KEY.FIELD_ENCODING_MAP, fieldEncodings);

        return p;
    }
    
    
    /**
     * @return a Map that can be used as the value for a Parameter
     * object's KEY.INFERRED_FIELDS key, to classify the specified
     * field with the specified Classifier type.
     */
    public static Map<String, Class<? extends Classifier>> getInferredFieldsMap(
            String field, Class<? extends Classifier> classifier) {
        Map<String, Class<? extends Classifier>> inferredFieldsMap = new HashMap<>();
        inferredFieldsMap.put(field, classifier);
        return inferredFieldsMap;
    }

    
    /**
     * Returns the default parameters used for the "dayOfWeek" encoder and algorithms.
     * @return
     */
    public static Parameters getParameters() {
        Parameters parameters = Parameters.getAllDefaultParameters();
        parameters.set(Parameters.KEY.INPUT_DIMENSIONS, new int[] { 8 });
        parameters.set(Parameters.KEY.COLUMN_DIMENSIONS, new int[] { 20 });
        parameters.set(Parameters.KEY.CELLS_PER_COLUMN, 6);
        
        //SpatialPooler specific
        parameters.set(Parameters.KEY.POTENTIAL_RADIUS, 12);//3
        parameters.set(Parameters.KEY.POTENTIAL_PCT, 0.5);//0.5
        parameters.set(Parameters.KEY.GLOBAL_INHIBITION, false);
        parameters.set(Parameters.KEY.LOCAL_AREA_DENSITY, -1.0);
        parameters.set(Parameters.KEY.NUM_ACTIVE_COLUMNS_PER_INH_AREA, 5.0);
        parameters.set(Parameters.KEY.STIMULUS_THRESHOLD, 1.0);
//        parameters.set(Parameters.KEY.SYN_PERM_INACTIVE_DEC, 0.01);
//        parameters.set(Parameters.KEY.SYN_PERM_ACTIVE_INC, 0.1);
        parameters.set(Parameters.KEY.SYN_PERM_INACTIVE_DEC, 0.007);
        parameters.set(Parameters.KEY.SYN_PERM_ACTIVE_INC, 0.15);
        parameters.set(Parameters.KEY.SYN_PERM_TRIM_THRESHOLD, 0.05);
        parameters.set(Parameters.KEY.SYN_PERM_CONNECTED, 0.1);
        parameters.set(Parameters.KEY.MIN_PCT_OVERLAP_DUTY_CYCLES, 0.1);
        parameters.set(Parameters.KEY.MIN_PCT_ACTIVE_DUTY_CYCLES, 0.1);
        parameters.set(Parameters.KEY.DUTY_CYCLE_PERIOD, 10);
        parameters.set(Parameters.KEY.MAX_BOOST, 10.0);
        parameters.set(Parameters.KEY.SEED, 42);
        
        //Temporal Memory specific
        parameters.set(Parameters.KEY.INITIAL_PERMANENCE, 0.2);
        parameters.set(Parameters.KEY.CONNECTED_PERMANENCE, 0.8);
        parameters.set(Parameters.KEY.MIN_THRESHOLD, 5);
        parameters.set(Parameters.KEY.MAX_NEW_SYNAPSE_COUNT, 6);
//        parameters.set(Parameters.KEY.PERMANENCE_INCREMENT, 0.05);
//        parameters.set(Parameters.KEY.PERMANENCE_DECREMENT, 0.05);
        parameters.set(Parameters.KEY.PERMANENCE_INCREMENT, 0.07);
        parameters.set(Parameters.KEY.PERMANENCE_DECREMENT, 0.025);
        parameters.set(Parameters.KEY.ACTIVATION_THRESHOLD, 4);
        
        return parameters;
    }
    
}
