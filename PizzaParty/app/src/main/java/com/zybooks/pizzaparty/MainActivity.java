package com.zybooks.pizzaparty;

import static android.widget.TextView.BufferType.NORMAL;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;


public class MainActivity extends AppCompatActivity {

    public final int SLICES_PER_PIZZA = 8;

    private EditText mNumAttendEditText;
    private TextView mNumPizzasTextView;

    private Spinner mSpinner;

    private PizzaCalculator.HungerLevel hungerLevel;

    private final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate was called");
        mNumAttendEditText = findViewById(R.id.num_attend_edit_text);
        mNumPizzasTextView = findViewById(R.id.num_pizzas_text_view);
        mNumAttendEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String totalText = getString(R.string.total_pizzas_num, 0);
                mNumPizzasTextView.setText(totalText);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
            // Add the missing code here
        });
        mSpinner = findViewById(R.id.spinner_size);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sizes_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String totalText = getString(R.string.total_pizzas_num, 0);
                mNumPizzasTextView.setText(totalText);
                String item = (String)parent.getItemAtPosition(position);
                hungerLevel = PizzaCalculator.HungerLevel.RAVENOUS;

                if(item.equals("Light"))
                {
                    hungerLevel = PizzaCalculator.HungerLevel.LIGHT;
                } else if (item.equals("Medium")) {
                    hungerLevel = PizzaCalculator.HungerLevel.MEDIUM;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void calculateClick(View view) {

        // Get how many are attending the party

        // Get hunger level selection
        int numAttend;
        try {
            String numAttendStr = mNumAttendEditText.getText().toString();
            numAttend = Integer.parseInt(numAttendStr);
        }
        catch (NumberFormatException ex) {
            numAttend = 0;
        }

        PizzaCalculator calc = new PizzaCalculator(numAttend, hungerLevel);
        int totalPizzas = calc.getTotalPizzas();

        // Place totalPizzas into the string resource and display
        String totalText = getString(R.string.total_pizzas_num, totalPizzas);
        mNumPizzasTextView.setText(totalText);
    }
}