package com.andreassavva.expensemanager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.text.DecimalFormat;

//Fragmentet som innehåller en enkel miniräknare.

public class TabFragment2 extends Fragment {

    private static final String TAG = "AKS";

    private EditText calcResult;
    private String result = "";
    private double num1 = 0;
    private double num2 = 0;
    private char operator = 'c';

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_fragment_2, container, false);

        calcResult = (EditText) rootView.findViewById(R.id.calc_result);
        calcResult.setKeyListener(null);

        // Matchar variabler med knapparna.
        Button calc0 = (Button) rootView.findViewById(R.id.calc_btn_0);
        Button calc1 = (Button) rootView.findViewById(R.id.calc_btn_1);
        Button calc2 = (Button) rootView.findViewById(R.id.calc_btn_2);
        Button calc3 = (Button) rootView.findViewById(R.id.calc_btn_3);
        Button calc4 = (Button) rootView.findViewById(R.id.calc_btn_4);
        Button calc5 = (Button) rootView.findViewById(R.id.calc_btn_5);
        Button calc6 = (Button) rootView.findViewById(R.id.calc_btn_6);
        Button calc7 = (Button) rootView.findViewById(R.id.calc_btn_7);
        Button calc8 = (Button) rootView.findViewById(R.id.calc_btn_8);
        Button calc9 = (Button) rootView.findViewById(R.id.calc_btn_9);
        Button calcDot = (Button) rootView.findViewById(R.id.calc_btn_dot);
        Button calcAdd = (Button) rootView.findViewById(R.id.calc_btn_add);
        Button calcSub = (Button) rootView.findViewById(R.id.calc_btn_sub);
        Button calcDiv = (Button) rootView.findViewById(R.id.calc_btn_div);
        Button calcMul = (Button) rootView.findViewById(R.id.calc_btn_mul);
        Button calcClear = (Button) rootView.findViewById(R.id.calc_btn_clear);
        Button calcCalc = (Button) rootView.findViewById(R.id.calc_btn_calc);

        // Sätter samma OnClickListener till alla knappar.
        calc0.setOnClickListener(btnListener);
        calc1.setOnClickListener(btnListener);
        calc2.setOnClickListener(btnListener);
        calc3.setOnClickListener(btnListener);
        calc4.setOnClickListener(btnListener);
        calc5.setOnClickListener(btnListener);
        calc6.setOnClickListener(btnListener);
        calc7.setOnClickListener(btnListener);
        calc8.setOnClickListener(btnListener);
        calc9.setOnClickListener(btnListener);
        calcDot.setOnClickListener(btnListener);
        calcAdd.setOnClickListener(btnListener);
        calcSub.setOnClickListener(btnListener);
        calcDiv.setOnClickListener(btnListener);
        calcMul.setOnClickListener(btnListener);
        calcClear.setOnClickListener(btnListener);
        calcCalc.setOnClickListener(btnListener);

        return rootView;
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {

        //Här skapar jag metoden som används för miniräknaren.
        // Det blir en Listener för alla knappar med switch().
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.calc_btn_0:
                    result += "0";
                    break;
                case R.id.calc_btn_1:
                    result += "1";
                    break;
                case R.id.calc_btn_2:
                    result += "2";
                    break;
                case R.id.calc_btn_3:
                    result += "3";
                    break;
                case R.id.calc_btn_4:
                    result += "4";
                    break;
                case R.id.calc_btn_5:
                    result += "5";
                    break;
                case R.id.calc_btn_6:
                    result += "6";
                    break;
                case R.id.calc_btn_7:
                    result += "7";
                    break;
                case R.id.calc_btn_8:
                    result += "8";
                    break;
                case R.id.calc_btn_9:
                    result += "9";
                    break;
                case R.id.calc_btn_add:
                    // Plussar ihop resultaten.
                    if (result.equals("")) {
                        break;
                    }
                    if (num1 == 0) {
                        num1 = Double.parseDouble(result);
                    } else if (num2 == 0) {
                        num2 = Double.parseDouble(result);
                    } else {
                        switch (operator) {
                            case '+':
                                num1 = num1 + num2;
                                break;
                            case '-':
                                num1 = num1 - num2;
                                break;
                            case '*':
                                num1 = num1 * num2;
                                break;
                            case '/':
                                num1 = num1 / num2;
                                break;
                        }
                        num2 = 0;
                    }
                    operator = '+';
                    result = "";
                    break;
                case R.id.calc_btn_sub:

                    // Subtraherar resultaten.
                    if (result.equals("")) {
                        break;
                    }
                    if (num1 == 0) {
                        num1 = Double.parseDouble(result);
                    } else if (num2 == 0) {
                        num2 = Double.parseDouble(result);
                    } else {
                        switch (operator) {
                            case '+':
                                num1 = num1 + num2;
                                break;
                            case '-':
                                num1 = num1 - num2;
                                break;
                            case '*':
                                num1 = num1 * num2;
                                break;
                            case '/':
                                num1 = num1 / num2;
                                break;
                        }
                        num2 = 0;
                    }
                    operator = '-';
                    result = "";
                    break;
                case R.id.calc_btn_mul:

                    // Multiplicerar resultaten.
                    if (result.equals("")) {
                        break;
                    }
                    if (num1 == 0) {
                        num1 = Double.parseDouble(result);
                    } else if (num2 == 0) {
                        num2 = Double.parseDouble(result);
                    } else {
                        switch (operator) {
                            case '+':
                                num1 = num1 + num2;
                                break;
                            case '-':
                                num1 = num1 - num2;
                                break;
                            case '*':
                                num1 = num1 * num2;
                                break;
                            case '/':
                                num1 = num1 / num2;
                                break;
                        }
                        num2 = 0;
                    }
                    operator = '*';
                    result = "";
                    break;
                case R.id.calc_btn_div:

                    // Dividerar resultaten.
                    if (result.equals("")) {
                        break;
                    }
                    if (num1 == 0) {
                        num1 = Double.parseDouble(result);
                    } else if (num2 == 0) {
                        num2 = Double.parseDouble(result);
                    } else {
                        switch (operator) {
                            case '+':
                                num1 = num1 + num2;
                                break;
                            case '-':
                                num1 = num1 - num2;
                                break;
                            case '*':
                                num1 = num1 * num2;
                                break;
                            case '/':
                                num1 = num1 / num2;
                                break;
                        }
                        num2 = 0;
                    }
                    operator = '/';
                    result = "";
                    break;
                case R.id.calc_btn_dot:
                    result += ".";
                    break;
                case R.id.calc_btn_clear:
                    // Raderar all data
                    num1 = 0;
                    num2 = 0;
                    operator = 'c';
                    result = "";
                    break;
                case R.id.calc_btn_calc:
                    // Kalkylerar och visar resultatet.
                    if (result.equals("")) {
                        result="0";
                    }
                    num2 = Double.parseDouble(result);
                    double res = 0;
                    switch (operator) {
                        case 'c':
                            res = num1;
                            break;
                        case '+':
                            res = num1 + num2;
                            break;
                        case '-':
                            res = num1 - num2;
                            break;
                        case '*':
                            res = num1 * num2;
                            break;
                        case '/':
                            res = num1 / num2;
                            break;
                    }
                    if (res!=0) {
                        DecimalFormat df = new DecimalFormat("#.#");
                        result = df.format(res);
                    }
                    num1 = res;
                    num2 = 0;
                    operator = 'c';
                    break;
            }
            calcResult.setText(result);
        }
    };
}
