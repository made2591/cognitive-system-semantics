package uni.sc.util.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Classe di utilità
 * Created by Matteo on 15/04/15.
 */
public class MMToolbox {

    public void goOn(String prefix) {

        Scanner input = new Scanner(System.in);
        System.out.print(prefix+"Premi invio per andare avanti...");
        input.nextLine();

    }


    public double getInputDouble(double from, double to, double def) {

        Scanner input = new Scanner(System.in);
        boolean go_on = true;
        Double ret = def;
        try {
            while(go_on) {
                String dig = input.nextLine();
                if (dig.equals("\n") || dig.length() == 0) go_on = false;
                else {
                    ret = Double.parseDouble(dig);
                    if (ret >= from && ret <= to) {
                        go_on = false;
                    } else {
                        System.out.print("Inserisci un numero compreso tra " + from + " e " + to + ": ");
                        ret = def;
                    }
                }
            }
        } catch (Exception e) {
            System.out.print("Inserisci un numero compreso tra " + from + " e " + to + ": ");
        }
        return ret;
    }

    public int getInputInt(int from, int to, int def) {

        Scanner input = new Scanner(System.in);
        boolean go_on = true;
        int ret = def;
        try {
            while(go_on) {
                String dig = input.nextLine();
                if (dig.equals("\n") || dig.length() == 0) go_on = false;
                else {
                    ret = Integer.parseInt(dig);
                    if (ret >= from && ret <= to) {
                        go_on = false;
                    } else {
                        System.out.print("Inserisci un numero compreso tra " + from + " e " + to + ": ");
                        ret = def;
                    }
                }
            }
        } catch (Exception e) {
            System.out.print("Inserisci un numero compreso tra " + from + " e " + to + ": ");
        }
        return ret;
    }

    public String getInputString(boolean blank_permitted) {

        Scanner input = new Scanner(System.in);
        String ret = input.nextLine();
        if(!blank_permitted) {
            while (ret.equals("\n") || ret.length() == 0) {
                System.out.println("Inserisci almeno un carattere");
                ret = input.nextLine();
            }
        }
        return ret;

    }

    public String arrayOfWordsToStringSeparatedBySemiColumn(ArrayList<String> array) {

        int counter = 1;
        String toprint = "";
        for(String elem : array) {

            toprint += elem;
            if(counter < array.size()) toprint += "; ";
            counter += 1;

        }

        return toprint;

    }

    public String arrayOfWordsToStringSeparatedBySpecifiedSeparator(ArrayList<String> array, String separator) {

        int counter = 1;
        String toprint = "";
        for(String elem : array) {

            toprint += elem;
            if(counter < array.size()) toprint += separator;
            counter += 1;

        }

        return toprint;

    }

    public String arrayOfDoublesToStringSeparatedBySpecifiedSeparator(ArrayList<Double> array, String separator) {

        int counter = 1;
        String toprint = "";
        for(Double elem : array) {

            toprint += elem;
            if(counter < array.size()) toprint += separator;
            counter += 1;

        }

        return toprint;

    }

    public ArrayList<String> splitSentenceAsArrayListOfString(String sentence) {

        return new ArrayList<String>(Arrays.asList(sentence.trim().split(" ")));

    }

    public ArrayList<String> castPrimitiveArrayToArrayList(String[] arrayOfString) {

        return new ArrayList<String>(Arrays.asList(arrayOfString));

    }

}
