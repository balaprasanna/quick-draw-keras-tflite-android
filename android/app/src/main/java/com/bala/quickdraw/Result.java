package com.bala.quickdraw;

import java.util.ArrayList;

public class Result {

    private final int mNumber;

    private final float mProbability;
    private final long mTimeCost;

    public String getLabel() {
        return label;
    }

    public String label;
    public ArrayList<Integer> top3 = new ArrayList<Integer>();

    public Result(float[] probs, long timeCost) {
        mNumber = argmax(probs);
        mProbability = probs[mNumber];
        mTimeCost = timeCost;
        label = "";

        get3largestIndex(probs, probs.length);

    }

    public void get3largestIndex(float arr[], int arr_size)
    {
        int i;
        float first, second, third;

        if (arr_size < 3) { System.out.print(" Invalid Input ");return;}

        third = first = second = Float.MIN_VALUE;
        for (i = 0; i < arr_size ; i ++)
        {
            /* If current element is greater than
            first*/
            if (arr[i] > first)
            {
                third = second;
                second = first;
                first = arr[i];
            }

            /* If arr[i] is in between first and
            second then update second  */
            else if (arr[i] > second)
            {
                third = second;
                second = arr[i];
            }

            else if (arr[i] > third)
                third = arr[i];
        }

        System.out.println("Three largest elements are " +
                first + " " + second + " " + third);

        int firstIdx=0, secondIdx=0, thirdIdx = 0;
        for (int ii = 0; ii < arr.length; ii++) {
            if (arr[ii] == first) {
                firstIdx = ii;
            }
            if (arr[ii] == second) {
                secondIdx = ii;
            }
            if (arr[ii] == third) {
                thirdIdx = ii;
            }
        }

        top3.add( firstIdx );
        top3.add( secondIdx );
        top3.add( thirdIdx );

        System.out.println( "Three largest elements index are ->" +   top3.toString());
    }


    public void setLabel(String newlabel) {
        label = newlabel;
    }

    public int getNumber() {
        return mNumber;
    }

    public float getProbability() {
        return mProbability;
    }

    public long getTimeCost() {
        return mTimeCost;
    }

    private static int argmax(float[] probs) {
        int maxIdx = -1;
        float maxProb = 0.0f;
        for (int i = 0; i < probs.length; i++) {
            if (probs[i] > maxProb) {
                maxProb = probs[i];
                maxIdx = i;
            }
        }
        return maxIdx;
    }


}
