package com.andreassavva.expensemanager;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class Contact implements Serializable{

    //Klassen kontakt som matchar en kontakt i telefonen

    private String name;
    private long id;
    private String moneyOwed;
    private byte[] bitmapBytes;
    private ArrayList<String> phoneNumbers;
    private boolean isSelected = false;

    public Contact(Context mContext, long contactId, String moneyOwed) {
        this.id = contactId;
        this.moneyOwed = moneyOwed;
        phoneNumbers = new ArrayList<>();

        // Skapar en Cursor för att få en specifik kontakt från Contacts med en specifik id.
        final Uri uri = ContactsContract.Contacts.CONTENT_URI;
        final String[] projection = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
        };
        String selection = ContactsContract.Contacts._ID + "='" + id + "'";
        ContentResolver mContentResolver = mContext.getContentResolver();
        Cursor contactCursor = mContentResolver.query(uri, projection, selection, null, null);

        String thumbnailUriString = "";

        if (contactCursor != null) {
            try {
                while (contactCursor.moveToNext()) {
                    // Här får jag namn och bild, som är nästan alla variabler som jag behöver från Cursor:n
                    name = contactCursor.getString(contactCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                    thumbnailUriString = contactCursor.getString(contactCursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                    Cursor phones = mContentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = '" + id + "'", null, null);
                    while (phones.moveToNext()) {
                        //  Här får jag telefonnummer med en ny Cursor. Lägger allt i en ArrayList då det kan finnas flera.
                        String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        number = number.replace(" ","");
                        if (!phoneNumbers.contains(number)) {
                            phoneNumbers.add(number);
                        }
                    }
                    phones.close();
                }
            } finally {
                contactCursor.close();
            }
        }

        if (thumbnailUriString!=null) {
            // Skapar en byte array från bilden som jag fick från Cursor:n. Jag gör det så att ArrayList<Contact>
            // ska bli Serializable och ska kunna sparas.
            try {
                Bitmap tempBmp = MediaStore.Images.Media.getBitmap(mContentResolver, Uri.parse(thumbnailUriString));
                Bitmap bitmap = getCroppedBitmap(tempBmp);
                this.bitmapBytes = convert(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Använder ic_contact_picture av biblioteket med contact chips (https://github.com/klinker41/android-chips)
            // därför att det inte finns någon bild sparad i kontakten. Skapar en byte array
            try {
                Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_contact_picture);
                this.bitmapBytes = convert(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    // Mina getters och setters
    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public String getMoneyOwed() {
        return moneyOwed;
    }

    public byte[] getBitmapBytes() {
        return bitmapBytes;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public void setMoneyOwed(String moneyOwed) {
        this.moneyOwed = moneyOwed;
    }

    public ArrayList<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    // Skapar en bitmap i form av en cirkel istället för en rektangel.
    public static Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    // Metoden som skapar en byte array från en bitmap.
    public static byte[] convert(Bitmap bitmap) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
        byte[] array = stream.toByteArray();
        stream.close();
        return array;
    }
}
