package com.example.myappy2;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.os.Bundle;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {
    private EditText editText;
    private ListView listView;
    private DatabaseReference databaseReference;
    private String stringMessage;
    private byte encryptionkey[]={9,115,51,86,105,4,-31,-23,-68,88,17,20,3,-105,119,-53};
    private Cipher cipher,decipher;
    private SecretKeySpec secretKeySpec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText =findViewById(R.id.editText);
        listView=findViewById(R.id.listView);
        try {
            databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://enc3app-bf670-default-rtdb.firebaseio.com/");
            try {
                cipher = Cipher.getInstance("AES");
                decipher = Cipher.getInstance("AES");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (NoSuchPaddingException e) {
                throw new RuntimeException(e);
            }
            secretKeySpec = new SecretKeySpec(encryptionkey, "AES");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //stringMessage=null;
                    stringMessage = snapshot.getValue().toString();
                    stringMessage = stringMessage.substring(1, stringMessage.length() - 1);

                    String[] stringMessageArray = stringMessage.split(", ");
                    Arrays.sort(stringMessageArray);
                    String[] stringFinal = new String[stringMessageArray.length * 2];

                    for (int i = 0; i < stringMessageArray.length; i++) {
                        String[] stringKeyValue = stringMessageArray[i].split("=", 2);
                        stringFinal[2 * i] = (String) android.text.format.DateFormat.format("dd-MM-yyyy hh:mm:ss", Long.parseLong(stringKeyValue[0]));
                        try {
                            stringFinal[2 * i + 1] = AESDecryptionMethod(stringKeyValue[1]);
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    listView.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, stringFinal));
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void sendButton(View view){
        Date date= new Date();
        databaseReference.child(Long.toString(date.getTime())).setValue(AESEncryptionMethod(editText.getText().toString()));
        editText.setText("");
    }
    private String AESEncryptionMethod(String string){
     byte [] stringByte=string.getBytes();
     byte[]  encryptedByte=new byte[stringByte.length];

        try {
            cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        try {
            encryptedByte= cipher.doFinal(stringByte);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
        String returnString=null;
        try {
            returnString = new String(encryptedByte,"ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return returnString;
    }
    private String AESDecryptionMethod(String string) throws UnsupportedEncodingException {
        byte[] encryptedByte=string.getBytes("ISO-8859-1");
        String decryptedString=string;
        byte[] decryption;
        try {
            decipher.init(cipher.DECRYPT_MODE,secretKeySpec);
            decryption=decipher.doFinal(encryptedByte);
            decryptedString=new String(decryption);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }
        return decryptedString;

    }
}