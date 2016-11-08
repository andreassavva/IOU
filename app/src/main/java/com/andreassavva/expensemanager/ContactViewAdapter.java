package com.andreassavva.expensemanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

public class ContactViewAdapter extends RecyclerView.Adapter<ContactViewAdapter.ContactViewHolder> {

    //Adaptern till RecyclerView som visar alla kontakter som användaren är skyldig pengar till (eller tvärtom)
    //Matchar en Contact till en rad i listan
    private static final int REQUEST_EDIT_IOU = 2;
    private static final String TAG = "AKS";
    private static final String FILENAME = "contact_list.iou";

    private Context fragmentContext;
    private AppCompatActivity mActivity;
    private List<Contact> contactList;
    public ActionMode mActionMode = null;
    ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Allt som har med hur ActionMode beter sig hanteras här.
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.action_mode_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {

            // Här hanterar jag vad som händer när man klickar på knappen "Clear" i ActionMode
            // som kommer upp när man "väljer" (checks) en list_item. Raderar alla valda IOUs
            // och sparar contactList efter dialogen som frågar om man verkligen vill radera IOU:ar.
            switch (item.getItemId()) {
                case R.id.action_mode_paid:
                    AlertDialog.Builder clearBuilder = new AlertDialog.Builder(mActivity);
                    clearBuilder.setTitle("Clear IOU(s)?")
                            .setMessage("Tapping 'OK' will permanently clear IOU(s).")
                            .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    for (int i = contactList.size() - 1; i >= 0; i--) {
                                        if (contactList.get(i).isSelected()) {
                                            contactList.remove(i);
                                        }
                                    }
                                    FileOutputStream fos = null;
                                    ObjectOutputStream out = null;
                                    try {
                                        fos = mActivity.openFileOutput(FILENAME, mActivity.MODE_PRIVATE);
                                        out = new ObjectOutputStream(fos);
                                        out.writeObject(contactList);
                                        out.close();
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                    mode.finish();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Inget händer
                                }
                            }).show();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // Jag vill att inget ska vara valt när ActionMode slutar.
            for (Contact c : contactList) {
                c.setIsSelected(false);
            }
            mode = null;
            notifyDataSetChanged();
        }
    };

    public class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        // ViewHolder till ViewAdapter.

        public TextView contactName;
        public TextView contactMoneyOwed;
        public ImageView contactPhoto;
        public CheckBox isCheckedBox;

        public ContactViewHolder(View v) {
            super(v);
            contactName = (TextView) itemView.findViewById(R.id.contact_name);
            contactMoneyOwed = (TextView) itemView.findViewById(R.id.contact_money_owed);
            contactPhoto = (ImageView) itemView.findViewById(R.id.dialogSmsImage);
            isCheckedBox = (CheckBox) itemView.findViewById(R.id.listItemCheckBox);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            /* Implementerar en metod som gör så att om det finns en item som är vald, så väljer man
             * en till item med en click (inte long click alltså). Skönare att använda.
             */
            boolean everythingIsUnchecked = true;
            for (Contact c : contactList) {
                if (c.isSelected()) {
                    everythingIsUnchecked = false;
                }
            }
            if (!everythingIsUnchecked) {
                // Här kallas också OnCheckedChangeListener.
                isCheckedBox.setChecked(!isCheckedBox.isChecked());
            } else {
                // Om man klickar en gång och inget är valt, skickas man vidare till IouInformation.class för att se info om kontakten.
                Intent intent = new Intent(fragmentContext, IouInformation.class);
                intent.putExtra("contactList", (Serializable) contactList);
                intent.putExtra("contactName", contactList.get(getLayoutPosition()).getName());
                intent.putExtra("contactId", contactList.get(getLayoutPosition()).getId());
                intent.putExtra("contactPhotoBytes", contactList.get(getLayoutPosition()).getBitmapBytes());
                intent.putExtra("contactMoney", contactList.get(getLayoutPosition()).getMoneyOwed());
                mActivity.startActivityForResult(intent, REQUEST_EDIT_IOU);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (!isCheckedBox.isChecked()) {
                // Calls OnCheckedChangeListener.
                isCheckedBox.setChecked(true);
                return true;
            } else {
                return false;
            }
        }
    }

    public ContactViewAdapter(List<Contact> contactList, AppCompatActivity activity, Context context) {
        this.contactList = contactList;
        this.mActivity = activity;
        this.fragmentContext = context;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ContactViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ContactViewHolder contactViewHolder, final int position) {
        contactViewHolder.contactName.setText(contactList.get(position).getName());
        // Sätter korrekt text i TextView:n.
        int moneyOwedIntTemp = Integer.parseInt(contactList.get(position).getMoneyOwed());
        if (moneyOwedIntTemp > 0) {
            contactViewHolder.contactMoneyOwed.setText("You owe them: " + contactList.get(position).getMoneyOwed());
        } else {
            contactViewHolder.contactMoneyOwed.setText("They owe you: " + String.valueOf(moneyOwedIntTemp - (2 * moneyOwedIntTemp)));
        }
        // Skapar Bitmap
        Bitmap bitmap = convert(contactList.get(position).getBitmapBytes());
        contactViewHolder.contactPhoto.setImageBitmap(bitmap);
        // Sätter OnTouchListener.
        contactViewHolder.contactPhoto.setOnTouchListener(null);
        contactViewHolder.contactPhoto.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    contactViewHolder.isCheckedBox.setChecked(true);
                }
                return true;
            }
        });

        //Sätter onCheckedChangeListener till null innan jag sätter om CheckBox är checked eller inte.
        contactViewHolder.isCheckedBox.setOnCheckedChangeListener(null);
        contactViewHolder.isCheckedBox.setChecked(contactList.get(position).isSelected());

        if (contactViewHolder.isCheckedBox.isChecked()) {
            // Jag vet inte varför, men jag var tvungen att introducera if-satsen här
            // annars när onDestroyActionMode kallas, så blir inte alla checkboxes till
            // bilder (om det är många som var valda). Nu blir det så.
            contactViewHolder.contactPhoto.setVisibility(View.GONE);
            contactViewHolder.isCheckedBox.setVisibility(View.VISIBLE);
        } else {
            contactViewHolder.isCheckedBox.setVisibility(View.GONE);
            contactViewHolder.contactPhoto.setVisibility(View.VISIBLE);
        }

        contactViewHolder.isCheckedBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            //metoden som kollar vilka checkboxes är checked och vilka är inte det, baserat på variabeln isSelected i klassen Contact.
            // här kollar jag också om alla checkboxes är unchecked så avslutar jag actionmode:n.
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                contactList.get(position).setIsSelected(isChecked);
                boolean everythingIsUnchecked = true;
                for (Contact c : contactList) {
                    if (c.isSelected()) {
                        everythingIsUnchecked = false;
                    }
                }
                if (mActionMode == null) {
                    mActionMode = mActivity.startActionMode(mActionModeCallback);
                } else if (everythingIsUnchecked) {
                    mActionMode.finish();
                    mActionMode = null;
                }
                if (isChecked) {
                    contactViewHolder.contactPhoto.setVisibility(View.GONE);
                    contactViewHolder.isCheckedBox.setVisibility(View.VISIBLE);
                } else {
                    contactViewHolder.isCheckedBox.setVisibility(View.GONE);
                    contactViewHolder.contactPhoto.setVisibility(View.VISIBLE);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static Bitmap convert(byte[] array) {
        // Får en bitmap av en byte array
        return BitmapFactory.decodeByteArray(array, 0, array.length);
    }


}