package br.edu.fa7.trabalho1;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {

    static final String TAG = "TRABALHO1";

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_SELECT_CONTACT = 2;

    private ImageView mImageView;
    private ContentResolver contentResolver;
    private String selectedContactId;
    private Button btnCall;
    private TextView textView;
    private Button btnPhoto;
    private Button btnContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.imageView);
        mImageView.setOnClickListener(this);

        textView = (TextView) findViewById(R.id.textView);

        btnPhoto = (Button) findViewById(R.id.btn_photo);
        btnPhoto.setOnClickListener(this);

        btnContact = (Button) findViewById(R.id.btn_contact);
        btnContact.setOnClickListener(this);

        btnCall = (Button) findViewById(R.id.btn_call);
        btnCall.setOnClickListener(this);

        contentResolver = getContentResolver();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_photo:
                dispatchTakePictureIntent();
                break;

            case R.id.imageView:
                dispatchTakePictureIntent();
                break;

            case R.id.btn_contact:
                dispatchSelectContactIntent();
                break;

            case R.id.btn_call:
                dial();
                break;
        }
    }

    private void resolveIntent(Intent intent, int request) {
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, request);
        } else {
            Toast.makeText(this, "Nenhum aplicativo instalado para completar sua ação!", Toast.LENGTH_LONG).show();
        }

    }

    private void dispatchTakePictureIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        resolveIntent(intent, REQUEST_IMAGE_CAPTURE);
    }

    private void dispatchSelectContactIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);

        resolveIntent(intent, REQUEST_SELECT_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE: loadImage(data);
                    break;

                case REQUEST_SELECT_CONTACT: loadContact(data);
                    break;
            }
        } else {
            Toast.makeText(this, "Não foi possível completar sua ação.", Toast.LENGTH_LONG).show();
        }
    }

    private void loadImage(Intent data) {
        Bundle extras = data.getExtras();
        Bitmap imageBitmap = (Bitmap) extras.get("data");

        if (imageBitmap != null) {
            mImageView.setImageBitmap(imageBitmap);
        }
    }

    private void loadContact(Intent data) {
        Uri contactUri = data.getData();
        Cursor cursor = contentResolver.query(contactUri, null, null, null, null);

        if (cursor.moveToFirst()) {
            selectedContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String number = getPhoneNumber();

            if (name != null) {
                textView.setText(name);
            }

            if (number != null) {
                Log.i(TAG, number);
                btnCall.setEnabled(true);
            } else {
                btnCall.setEnabled(false);
            }
        }

        cursor.close();
    }

    private void dial() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + getPhoneNumber()));

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private String getPhoneNumber() {
        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + selectedContactId, null, null);

        while (cursor.moveToNext()) {
            String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            if (number != null) {
                return number;
            }
        }

        cursor.close();
        return null;
    }
}
