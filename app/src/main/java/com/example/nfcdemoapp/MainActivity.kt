package com.example.nfcdemoapp

import android.content.Context
import android.media.RingtoneManager
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import java.io.IOException

class MainActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {
//Ref VIDEO --- https://www.youtube.com/watch?v=hca1_yzT66A&ab_channel=FluxIT

    private lateinit var mNfcAdapter: NfcAdapter
    private lateinit var mUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mTextContent = findViewById<EditText>(R.id.mTextContent)
        val mBtnSend = findViewById<Button>(R.id.writeTagBtn)

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)

        //To check that the device has NFC technology
        if (mNfcAdapter == null) {
            Toast.makeText(this, "This device does not have NFC technology.", Toast.LENGTH_LONG)
                .show()
        } else {
            //We check that the NFC antenna is on/enabled
            if (!mNfcAdapter.isEnabled) {
                Toast.makeText(this, "Nfc antenna disabled.", Toast.LENGTH_LONG).show()
            }
        }

        mBtnSend.setOnClickListener { v ->
            mUrl = if(mTextContent.text.contains("https://www.") && mTextContent.text.contains(".com")){
                mTextContent.text.toString()
            }else{
                "${"https://www."}${mTextContent.text.trim()}${".com"}"
            }
            Toast.makeText(
                this,
                "URL created: $mUrl . Move the device close to the NFC tag",
                Toast.LENGTH_LONG
            ).show()
            v.hideKeyboard()
        }
    }

    override fun onTagDiscovered(tag: Tag) {
        // NFC Tags can support different technologies. In this case we will use Ndef Technology
        val mNdef = Ndef.get(tag);

        if (mNdef != null) {

            // We create an Ndef Record from a Uri with our Url
            val mRecord = NdefRecord.createUri(mUrl);

            // We add the NdefRecord to our NdefMessage
            val mNdefMsg = NdefMessage(mRecord)

            //We try to open a connection and write the Tag with our NdefMessage
            try {
                mNdef.connect();
                mNdef.writeNdefMessage(mNdefMsg);

                //If the Tag was written successfully show the Toast
                runOnUiThread {
                    Toast.makeText(this, "Tag escrito con éxito", Toast.LENGTH_SHORT).show()
                }

                // Make a Sound
                try {
                    val notification =
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    val ringtone = RingtoneManager.getRingtone(
                        applicationContext,
                        notification
                    );
                    ringtone.play();
                } catch (e: Exception) {
                    // Some error playing sound
                    runOnUiThread {
                        Toast.makeText(this, "Error trying to write", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

            } catch (e: Exception) {
                //if the Tag is invalid
            } finally {
                // We close the connection with the Tag (Let's avoid errors and misuse of resources)
                try {
                    mNdef.close();
                } catch (e: IOException) {
                    // We show a message in case the operation has been interrupted
                    Toast.makeText(this, "Error operation has been interrupted", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } else {
            Toast.makeText(this, "Tag Invalid", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mNfcAdapter != null) {
            val options = Bundle()
            //We add a few extra milliseconds for the Tag to be read correctly
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)

            //We enable the reading mode to detect the Tag
            mNfcAdapter.enableReaderMode(
                this,
                this,
                NfcAdapter.FLAG_READER_NFC_A or
                        NfcAdapter.FLAG_READER_NFC_B or
                        NfcAdapter.FLAG_READER_NFC_F or
                        NfcAdapter.FLAG_READER_NFC_V,
                options
            )
        }
    }

    override fun onPause() {
        super.onPause()
        //We disabled the reading mode so that we only detect Tags with the App in the foreground
        mNfcAdapter.disableReaderMode(this);
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

}