package com.andreassavva.expensemanager;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class IouInformation extends AppCompatActivity {

    // Aktiviteten som startas när man klickar på en item i listan.
    // Visar information för IOU:n

    private static final String TAG = "AKS";
    private static final String FILENAME = "contact_list.iou";

    private ArrayList<String> phoneNumbers;
    private long contactId;
    private String contactName, contactMoney;
    private byte[] contactPhotoBytes;
    private String contactNumber;
    Drawable contactImage;
    private List<Contact> contactList;
    private TextView nameText, moneyText, owesMoneyTextView;
    private ImageView contactImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iou_information);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("IOU");

        nameText = (TextView) findViewById(R.id.nameText);
        contactImg = (ImageView) findViewById(R.id.contactImg);
        moneyText = (TextView) findViewById(R.id.moneyText);
        owesMoneyTextView = (TextView) findViewById(R.id.owesMoneyTextView);

        // Får data från en intent om det finns.
        if (getIntent().getExtras() != null) {
            Intent intent = getIntent();
            contactList = (ArrayList<Contact>) intent.getSerializableExtra("contactList");
            contactId = intent.getLongExtra("contactId", 0);
            contactName = intent.getStringExtra("contactName");
            contactPhotoBytes = intent.getByteArrayExtra("contactPhotoBytes");
            contactMoney = intent.getStringExtra("contactMoney");
        }

        // Skapar en drawable för att visa som bild.
        ByteArrayInputStream is = new ByteArrayInputStream(contactPhotoBytes);
        contactImage = Drawable.createFromStream(is, "contactImg");

        nameText.setText(contactName);
        contactImg.setImageDrawable(contactImage);
        int moneyOwedIntTemp = Integer.parseInt(contactMoney);
        if (moneyOwedIntTemp > 0) {
            owesMoneyTextView.setText("You owe " + contactName + ":");
            moneyText.setText(contactMoney);
        } else {
            owesMoneyTextView.setText(contactName + " owes you:");
            moneyText.setText(String.valueOf(moneyOwedIntTemp - (2 * moneyOwedIntTemp)));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_iou_information, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.iou_info_menu_edit:
                // Om man väljer edit från Toolbar. Skapar en Dialog som visar namnet och en EditText för
                // att redigera IOU:n.
                LayoutInflater inflater = getLayoutInflater();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final View view = inflater.inflate(R.layout.dialog_edit, null);
                builder.setTitle("Edit IOU")
                        .setView(view)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // Sparar den nya contactList vid slutet.
                                TextView tempMoneyTextView = (TextView) view.findViewById(R.id.dialogMoneyEditText);
                                RadioButton dialogYouOweThem = (RadioButton) view.findViewById(R.id.dialogYouOweThem);
                                String moneyOwedTemp = tempMoneyTextView.getText().toString();
                                moneyText.setText(moneyOwedTemp);
                                if (dialogYouOweThem.isChecked()) {
                                    owesMoneyTextView.setText("You owe " + contactName + ":");
                                } else {
                                    owesMoneyTextView.setText(contactName + " owes you:");
                                    moneyOwedTemp = "-" + moneyOwedTemp;
                                }

                                for (int i = contactList.size() - 1; i >= 0; i--) {
                                    if (contactList.get(i).getId() == contactId) {
                                        contactList.get(i).setMoneyOwed(moneyOwedTemp);
                                    }
                                }

                                for (int i = 0; i < contactList.size(); i++) {
                                    contactList.get(i).setIsSelected(false);
                                }

                                FileOutputStream fos = null;
                                ObjectOutputStream out = null;
                                try {
                                    fos = getApplicationContext().openFileOutput(FILENAME, getApplicationContext().MODE_PRIVATE);
                                    out = new ObjectOutputStream(fos);
                                    out.writeObject(contactList);
                                    out.close();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }).show();
                break;
            case R.id.iou_info_menu_sms:
                // När man trycker "Sms" i Toolbar:en. Först byggs en dialog om det finns 2 eller flera telefonnummer
                // associerade med kontakten för att användaren ska välja vilken som är rätt. Kollar även för dubbletter.
                // Efteråt startas metoden sendSms.
                phoneNumbers = new ArrayList<>();
                for (int i = contactList.size() - 1; i >= 0; i--) {
                    if (contactList.get(i).getId() == contactId) {
                        phoneNumbers = contactList.get(i).getPhoneNumbers();
                    }
                }

                if (phoneNumbers.isEmpty()) {
                    Toast.makeText(this, "Contact has no phone number saved", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (phoneNumbers.size() > 1) {
                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);

                    builderSingle.setIcon(contactImage);
                    builderSingle.setTitle(contactName);

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice);
                    for (String s : phoneNumbers) {
                        arrayAdapter.add(s);
                    }

                    builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            contactNumber = phoneNumbers.get(which);
                            sendSms(contactNumber).show();
                        }
                    });
                    builderSingle.show();
                } else {
                    contactNumber = phoneNumbers.get(0);
                    sendSms(contactNumber).show();
                }

                break;
            case R.id.iou_info_menu_clear:
                // När man väljer att radera IOU:n. Skapar en dialog som frågar användaren om man verkligen
                // vill radera den.
                AlertDialog.Builder clearBuilder2 = new AlertDialog.Builder(this);
                clearBuilder2.setTitle("Clear IOU?")
                        .setMessage("Tapping 'OK' will permanently clear IOU.")
                        .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                for (int i = contactList.size() - 1; i >= 0; i--) {
                                    if (contactList.get(i).getId() == contactId) {
                                        contactList.remove(i);
                                    }
                                }
                                FileOutputStream fos = null;
                                ObjectOutputStream out = null;
                                try {
                                    fos = openFileOutput(FILENAME, MODE_PRIVATE);
                                    out = new ObjectOutputStream(fos);
                                    out.writeObject(contactList);
                                    out.close();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public AlertDialog.Builder sendSms(final String phoneNo) {
        // Metoden som skickar sms till den som man är skyldig till (eller tvärtom). Skapar en Toast när sms:et håller på att
        // skickas, och en Toast när det är skickat klart.
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_sms, null);

        AlertDialog.Builder smsBuilder = new AlertDialog.Builder(this);

        ImageView contactImageView = (ImageView) view.findViewById(R.id.dialogSmsImage);
        TextView nameTextView = (TextView) view.findViewById(R.id.dialogSmsName);
        TextView numberTextView = (TextView) view.findViewById(R.id.dialogSmsNumber);
        final EditText messageEditText = (EditText) view.findViewById(R.id.dialogSmsMessage);

        contactImageView.setImageDrawable(contactImage);
        nameTextView.setText(contactName);
        numberTextView.setText(phoneNo);
        int moneyTemp = Integer.parseInt(contactMoney);
        if (moneyTemp>0) {
            messageEditText.setText("Hey, just sent you a message to remind you that I owe you " + contactMoney + " :) Thanks for the loan!");
        } else {
            contactMoney = String.valueOf(moneyTemp - 2*moneyTemp);
            messageEditText.setText("Hey, just sent you a message to kindly remind you that you owe me " + contactMoney + " :)");
        }
        messageEditText.setSelection(messageEditText.getText().length());

        smsBuilder.setTitle("Send SMS")
                .setView(view)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String SENT = "SMS_SENT";
                        PendingIntent sentPI = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(SENT), 0);

                        getApplicationContext().registerReceiver(
                                new BroadcastReceiver() {
                                    @Override
                                    public void onReceive(Context arg0, Intent arg1) {
                                        switch (getResultCode()) {
                                            case Activity.RESULT_OK:
                                                Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT).show();
                                                break;
                                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                                Toast.makeText(getApplicationContext(), "Message failed to send", Toast.LENGTH_SHORT).show();
                                                break;
                                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                                                Toast.makeText(getApplicationContext(), "No service", Toast.LENGTH_SHORT).show();
                                                break;
                                            case SmsManager.RESULT_ERROR_NULL_PDU:
                                                Toast.makeText(getApplicationContext(), "Message failed to send", Toast.LENGTH_SHORT).show();
                                                break;
                                            case SmsManager.RESULT_ERROR_RADIO_OFF:
                                                Toast.makeText(getApplicationContext(), "Message failed to send", Toast.LENGTH_SHORT).show();
                                                break;
                                        }
                                    }
                                }, new IntentFilter(SENT));

                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phoneNo, null, messageEditText.getText().toString(), sentPI, null);
                        Toast.makeText(getApplicationContext(), "Sending message...", Toast.LENGTH_SHORT).show();
                    }
                });
        return smsBuilder;
    }


}
