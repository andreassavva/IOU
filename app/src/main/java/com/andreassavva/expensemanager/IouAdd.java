package com.andreassavva.expensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.RecipientEditTextView;
import com.android.ex.chips.recipientchip.DrawableRecipientChip;

public class IouAdd extends AppCompatActivity {

    // Den här är aktiviteten som startas när man trycker på FAB "Add" i TabFragment1.
    // Presenterar en GUI med information som krävs för att addera en ny IOU.

    private static final String TAG = "AKS";

    private EditText moneyOwedEditText;
    private RecipientEditTextView phoneRetv;
    private Button showAllBtn;
    private RadioButton youOweThem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iou_add);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);

        // Matchar variabler med objekten.
        youOweThem = (RadioButton) findViewById(R.id.youOweThem);
        moneyOwedEditText = (EditText) findViewById(R.id.moneyOwedEditText);

        // Den här är en RecipientEditTextView som visar contact chips för alla kontakter som man har i telefonen.
        // Jag fick biblioteket på nätet.
        phoneRetv = (RecipientEditTextView) findViewById(R.id.contactEditText);
        phoneRetv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        BaseRecipientAdapter adapter = new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE, this);
        adapter.setShowMobileOnly(true);
        phoneRetv.setAdapter(adapter);
        phoneRetv.dismissDropDownOnItemSelected(true);

        // Knappen som visar alla kontakter.
        showAllBtn = (Button) findViewById(R.id.showAllBtn);
        showAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneRetv.showAllContacts();
                phoneRetv.dismissDropDownOnItemSelected(true);
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_iou_add, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // När man väljer nånting från menyn i Toolbar:en
        switch (item.getItemId()) {
            case R.id.iou_add_menu_done:
                // Adderar IOU i listan. Också gör nödvändiga säkerhetskontroll
                // (så att användaren inte kan addera en problemtisk kontakt).
                long contactId;
                String moneyOwed;
                DrawableRecipientChip[] chips = phoneRetv.getSortedRecipients();
                if (chips.length==1 && chips[0].getEntry().getContactId()!=-1) {
                    contactId = chips[0].getContactId();
                    moneyOwed = moneyOwedEditText.getText().toString();
                    if (!moneyOwed.equals("")) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("contactId", contactId);
                        if (youOweThem.isChecked()) {
                            returnIntent.putExtra("moneyOwed", moneyOwed);
                        } else {
                            returnIntent.putExtra("moneyOwed", "-" + moneyOwed);
                        }
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    } else if (moneyOwed.equals("")) {
                        Toast.makeText(getApplicationContext(), "Fill in the money owed", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter one valid contact in the name field", Toast.LENGTH_LONG).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}

