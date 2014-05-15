package com.muzima.utils.audio;

import java.io.File;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.muzima.R;
import com.muzima.utils.MediaUtils;
import com.muzima.view.forms.AudioComponent;

import static com.muzima.utils.Constants.APP_AUDIO_DIR;

public class AudioIntent extends Activity {
	private final static String TAG = "AudioIntent";
	
	public static final String KEY_AUDIO_PATH = "audioPath";
    public static final String KEY_AUDIO_CAPTION = "audioCaption";
    public static final String KEY_SECTION_NAME = "sectionName";

	private final int AUDIO_CAPTURE = 1;
	private final int AUDIO_CHOOSE = 2;
	
    private String AUDIO_FOLDER;
    private boolean isNewAudio;

    private TextView mNoAudioMessage;
    private ImageView mAudioThumbnail;
    private EditText mAudioCaption;
    private View mAudioPreview;
    private View mAudioAcceptContainer;
    private View mAudioRecordContainer;

    private String mSectionName;
    private String mBinaryName;
    private String mBinaryDescription;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        
        Intent i = getIntent();
        String formUuid = i.getStringExtra(AudioComponent.FORM_UUID);
        String audioPath = i.getStringExtra(KEY_AUDIO_PATH);
        mBinaryDescription  = i.getStringExtra(KEY_AUDIO_CAPTION);
        mSectionName = i.getStringExtra(KEY_SECTION_NAME);

        if (formUuid != null)
        	AUDIO_FOLDER = APP_AUDIO_DIR + "/" + formUuid;
        else
        	AUDIO_FOLDER = APP_AUDIO_DIR;

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_AUDIO_PATH))
                mBinaryName = savedInstanceState.getString(KEY_AUDIO_PATH);
        } else {
        	if (audioPath != null) {
	            File audio = new File(audioPath);
	            if (audio.exists())
	                mBinaryName = audio.getName();
        	}
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_AUDIO_PATH))
                mBinaryName = savedInstanceState.getString(KEY_AUDIO_PATH);

            if (savedInstanceState.containsKey(KEY_AUDIO_CAPTION))
                mBinaryDescription = savedInstanceState.getString(KEY_AUDIO_CAPTION);

            if (savedInstanceState.containsKey(KEY_SECTION_NAME))
                mSectionName = savedInstanceState.getString(KEY_SECTION_NAME);

        } else {
            if (audioPath != null) {
                File audio = new File(audioPath);
                if (audio.exists()) {
                    mBinaryName = audio.getName();
                    isNewAudio = false;
                }
            }
        }
        
        mNoAudioMessage = (TextView) findViewById(R.id.noAudioMessage);
        mAudioPreview = (View) findViewById(R.id.audioPreview);
        mAudioCaption = (EditText) findViewById(R.id.audioCaption);
        mAudioThumbnail = (ImageView) findViewById(R.id.audioThumbnail);
        mAudioAcceptContainer = (View) findViewById(R.id.audioAcceptContainer);
        mAudioRecordContainer = (View) findViewById(R.id.audioRecordContainer);
        
        refreshAudioView();
	}
	
	public void acceptAudio(View view) {
    	String caption = mAudioCaption.getText().toString();
    	
    	if (caption == null || caption.length() < 1){
    		Toast.makeText(getApplicationContext(),"Please enter a caption for the audio", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	String audioUri = AUDIO_FOLDER + File.separator  + mBinaryName;

        if (mBinaryName != null) {
            Intent i = new Intent();
            i.putExtra(KEY_SECTION_NAME, mSectionName);
            i.putExtra(KEY_AUDIO_PATH, audioUri);
            i.putExtra(KEY_AUDIO_CAPTION, caption);
            setResult(RESULT_OK, i);
        }
        finish();
	}
	
	public void rejectAudio(View view) {
		if (isNewAudio) 
			deleteMedia();
			
		mBinaryName=null;
		
		refreshAudioView();
	}
	
	public void recordAudio(View view) {
		isNewAudio = true;
		Intent i = new Intent(Audio.Media.RECORD_SOUND_ACTION);
		i.putExtra(MediaStore.EXTRA_OUTPUT,
				Audio.Media.EXTERNAL_CONTENT_URI.toString());
		try {
			startActivityForResult(i, AUDIO_CAPTURE);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this,"Error: Activity for recording audio not found", Toast.LENGTH_SHORT).show();
		}		
	}

    public void chooseAudio(View view) {
        isNewAudio = false;
        Intent i;
        final boolean isKitKat = Build.VERSION.SDK_INT >= 19;

        if (isKitKat){
            i = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            i.addCategory(Intent.CATEGORY_OPENABLE);
        } else
            i = new Intent(Intent.ACTION_GET_CONTENT);

        try {
            i.setType("audio/*");
            startActivityForResult(i,AUDIO_CHOOSE);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG,e.getMessage());
			Toast.makeText(this,"Error: Activity for choosing audio not found", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void playAudio(View view) {
		Intent i = new Intent("android.intent.action.VIEW");
		File f = new File(AUDIO_FOLDER + File.separator + mBinaryName);
		i.setDataAndType(Uri.fromFile(f), "audio/*");
		try {
			startActivity(i);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(AudioIntent.this,"Error: Activity for playing audio not found", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void refreshAudioView() {
		if (mBinaryName != null) {
			// show preview with thumbnail view
            mAudioPreview.setVisibility(View.VISIBLE);
            
            // show accept view
            mAudioAcceptContainer.setVisibility(View.VISIBLE);

            // show caption view
            mAudioCaption.setVisibility(View.VISIBLE);
            
            //hide record view
            mAudioRecordContainer.setVisibility(View.GONE);
            
            //hide no message view
            mNoAudioMessage.setVisibility(View.GONE);

            if (mBinaryDescription != null)
                mAudioCaption.setText(mBinaryDescription);
		} else {
			mAudioThumbnail.setImageBitmap(null);
			
			// hide preview with thumbnail view
            mAudioPreview.setVisibility(View.GONE);
            
            // hide accept view
            mAudioAcceptContainer.setVisibility(View.GONE);

            // hide caption view
            mAudioCaption.setVisibility(View.GONE);
            
            //show record view
            mAudioRecordContainer.setVisibility(View.VISIBLE);
            
            //show no message view
            mNoAudioMessage.setVisibility(View.VISIBLE);
		}
	}

    private void deleteMedia() {
    	//delete from media provider
        int del = MediaUtils.deleteAudioFileFromMediaProvider(this, AUDIO_FOLDER + File.separator + mBinaryName);
    	Log.i(TAG, "Deleted " + del + " rows from media content provider");
    }

	public void saveAudio(String audioPath) {
		if (mBinaryName != null)
			deleteMedia();

		String extension = audioPath.substring(audioPath.lastIndexOf("."));
		String destAudioPath = AUDIO_FOLDER + File.separator + System.currentTimeMillis() + extension;

		File source = new File(audioPath);
		File newAudio = new File(destAudioPath);
		if (MediaUtils.folderExists(AUDIO_FOLDER))
            MediaUtils.copyFile(source, newAudio);

		if (newAudio.exists()) {
			ContentValues values = new ContentValues(6);
			values.put(Audio.Media.TITLE, newAudio.getName());
			values.put(Audio.Media.DISPLAY_NAME, newAudio.getName());
			values.put(Audio.Media.DATE_ADDED, System.currentTimeMillis());
			values.put(Audio.Media.DATA, newAudio.getAbsolutePath());

			Uri AudioURI = getContentResolver().insert(
					Audio.Media.EXTERNAL_CONTENT_URI, values);
			Log.i(TAG, "Inserting Audio returned uri = " + AudioURI.toString());
		} else
			Log.e(TAG, "Inserting Audio file FAILED");

		mBinaryName = newAudio.getName();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (intent != null) {
            final boolean isKitKat = Build.VERSION.SDK_INT >= 19;

            boolean openDocument = false;
            if (requestCode == AUDIO_CHOOSE)
                openDocument = true;

            // get the file path and create a copy in the instance folder
            String audioPath = getPathFromUri((Uri) intent.getData(), isKitKat, openDocument);
            saveAudio(audioPath);
            refreshAudioView();
        }
	}
	
    @Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
        outState.putString(KEY_SECTION_NAME, mSectionName);
        outState.putString(KEY_AUDIO_PATH, mBinaryName);
        outState.putString(KEY_AUDIO_CAPTION, mBinaryDescription);
	}

    private String getPathFromUri(Uri uri, boolean isKitKat, boolean isOpenDocument) {
        if (uri.toString().startsWith("file"))
            return uri.toString().substring(6);
        else {
            String[] audioProjection = {Audio.Media.DATA };
            String audioSelection = null;
            Cursor c = null;

            try {
                if (isKitKat && isOpenDocument) {
                    String id = uri.getLastPathSegment().split(":")[1];
                    audioSelection = Audio.Media._ID + "=" + id;
                    uri = getUri();
                }

                c = getContentResolver().query(uri, audioProjection, audioSelection, null, null);
                int column_index = c.getColumnIndexOrThrow(Audio.Media.DATA);
                String audioPath = null;
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    audioPath = c.getString(column_index);
                }
                return audioPath;
            } finally {
                if (c != null)
                    c.close();
            }
        }
    }

    // Get the Uri of Internal/External Storage for Media
    private Uri getUri() {
        String state = Environment.getExternalStorageState();
        if(!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return MediaStore.Audio.Media.INTERNAL_CONTENT_URI;

        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }
}
