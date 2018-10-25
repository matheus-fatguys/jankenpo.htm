/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.matheus.fatguys.htm.net;

/**
 *
 * @author y2gh
 */
public class PredictionsUtil {

    private static int[] steps;
    
    public static void setSteps(int[] steps) {
        PredictionsUtil.steps = steps;
    }

    public static int[] getSteps() {
        return PredictionsUtil.steps;
    }
    
}
