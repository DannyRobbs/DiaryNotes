package com.example.dairynotes;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class NoteItemFragment extends Fragment implements noteAdapter.onItemClick, noteAdapter.updateSeek {
    private static final String AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO;
    ArrayList<notesModel> note = new ArrayList<>();
    RecyclerView recyclerView;
    View lay, noteTitleLay, root;
    ImageView button, sendBtn, imageBtn;
    int position;
    TextView title, date;
    EditText noteArea, notetitle;
    noteAdapter adapter;
    Toolbar tb;
    int adapterposition;
    MediaPlayer mediaPlayer;
    String audioFilePath;
    MediaRecorder mediaRecorder;
    ImageButton okBtn;
    SimpleDateFormat df;
    Boolean newfile, isPlaying = false;

    public NoteItemFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_note_item, container, false);
        init();
        setListener();
        ((MainActivity) getActivity()).setSupportActionBar(tb);
        return root;
    }

    private void init() {
        button = root.findViewById(R.id.audio);
        date = root.findViewById(R.id.date);
        tb = root.findViewById(R.id.toolbar);
        okBtn = root.findViewById(R.id.okbtn);
        noteTitleLay = root.findViewById(R.id.notetitlelay);
        sendBtn = root.findViewById(R.id.sendbtn);
        imageBtn = root.findViewById(R.id.imagebtn);
        notetitle = root.findViewById(R.id.notetitle);
        df = new SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault());
        date.setText(df.format(new Date()));
        lay = root.findViewById(R.id.mainlay);
        noteArea = root.findViewById(R.id.note_edittext);
        recyclerView = root.findViewById(R.id.note_item_recycler);
        title = root.findViewById(R.id.title_home);

        newfile = getArguments().getBoolean("newfile");
        if (newfile) {
            noteTitleLay.setVisibility(View.VISIBLE);
        }
        position = getArguments().getInt("position");
        note = getArguments().getParcelableArrayList("mynote");
        if (!newfile) {
            lay.setVisibility(View.GONE);
        }
        assert note != null;
        if (note.size() > 0 && !newfile) {
            title.setText(note.get(position).getTitle());
            // title.setText(note.get(position).getTitle());
            adapter = new noteAdapter(getActivity(), note.get(position).getNoteItemList(), this, this);
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
            recyclerView.setAdapter(adapter);
        } else {
            title.setText("untitled");
        }
    }

    private void setListener() {
        button.setOnTouchListener((v, event) -> {
            if (checkPermissions()) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startRecording();
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        stopRecording();
                        break;
                }
            }
            return false;
        });
        sendBtn.setOnClickListener(v -> {

            if (noteTitleLay.getVisibility() == View.VISIBLE) {
                Toast.makeText(getActivity(), "Title Empty", Toast.LENGTH_SHORT).show();
            } else {

                if (!noteArea.getText().toString().isEmpty()) {
                    note.get(getPosition()).addItemToNote(noteArea.getText().toString(), "string", "null");
                    adapter.notifyDataSetChanged();
                    noteArea.setText("");
                }

                if (note.get(getPosition()).getNoteItemList().size() > 0) {
                    lay.setVisibility(View.GONE);
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(noteArea.getWindowToken(), 0);
                }
            }
            writeFile();

        });
        okBtn.setOnClickListener(v -> {
            if (!notetitle.getText().toString().isEmpty()) {
                note.add(new notesModel(notetitle.getText().toString()));
                title.setText(notetitle.getText().toString());
                writeFile();
                noteTitleLay.setVisibility(View.GONE);
                adapter = new noteAdapter(getActivity(), note.get(note.size() - 1).getNoteItemList(), this, this);
                recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                recyclerView.setAdapter(adapter);
            } else {
                Toast.makeText(getActivity(), "Title Empty", Toast.LENGTH_SHORT).show();
            }
        });
        imageBtn.setOnClickListener(v -> {

            if (noteTitleLay.getVisibility() == View.VISIBLE) {
                Toast.makeText(getActivity(), "Title Empty", Toast.LENGTH_SHORT).show();
            } else {

                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "Select an Image"), 1);
            }
            writeFile();


        });
    }

    private void stopRecording() {
        mediaRecorder.release();
        mediaRecorder = null;
        if (!noteArea.getText().toString().isEmpty()) {
            note.get(getPosition()).addItemToNote(audioFilePath, "audio", noteArea.getText().toString());
        } else {
            note.get(getPosition()).addItemToNote(audioFilePath, "audio", "null");
        }
        noteArea.setText("");
        writeFile();
        adapter.notifyDataSetChanged();
    }

    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        String filePath = getActivity().getExternalFilesDir("/").getAbsolutePath();
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.CANADA);
        String fileName = note.get(getPosition()).getTitle() + "_" + format.format(new Date()) + ".3gp";
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(filePath + "/" + fileName);
        audioFilePath = filePath + "/" + fileName;
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaRecorder.start();
    }

    private int getPosition() {
        int num = 0;
        if (newfile) {
            num = note.size() - 1;
        } else {
            num = position;
        }
        return num;
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(getActivity(), AUDIO_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{AUDIO_PERMISSION}, 1);
            return false;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == getActivity().RESULT_OK) {
            Uri uri = data.getData();
            try {
                Bitmap bitmp = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                if (!noteArea.getText().toString().isEmpty()) {
                    note.get(getPosition()).addItemToNote(BitMapToString(bitmp), "image", noteArea.getText().toString());
                } else {
                    note.get(getPosition()).addItemToNote(BitMapToString(bitmp), "image", "null");
                }
                if (note.get(getPosition()).getNoteItemList().size() > 0) {
                    lay.setVisibility(View.GONE);
                }
                noteArea.setText("");
                adapter.notifyDataSetChanged();
                writeFile();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(noteArea.getWindowToken(), 0);
            } catch (IOException e) {
//
            }
        }
    }

    public void writeFile() {
        Gson gson = new Gson();
        String array = gson.toJson(note);
        try {
            FileOutputStream fileOutputStream = getActivity().openFileOutput("Dairynotes.txt", Context.MODE_PRIVATE);
            fileOutputStream.write(array.getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    @Override
    public void OnClickListener(customType n, int position, String type) {
        adapterposition = position;
        if (type.equalsIgnoreCase("audio")) {
            refreshProgress(getPosition(), adapterposition, 0, 0);
            if (isPlaying) {
                stopPlaying();
                startPlaying((String) n.getMyObject());
            } else {
                startPlaying((String) n.getMyObject());
            }
        }

    }

    private void stopPlaying() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        refreshProgress(getPosition(), adapterposition, 0, 0);
        isPlaying = false;
    }

    private void startPlaying(String file) {
        isPlaying = true;
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(file);
            mediaPlayer.prepare();
            mediaPlayer.start();
            Handler mHandler = new Handler();
//Make sure you update Seekbar on UI thread
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mediaPlayer != null) {

                        int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                        refreshProgress(getPosition(), adapterposition, mCurrentPosition, mediaPlayer.getDuration());
                    }
                    mHandler.postDelayed(this, 1000);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(int progress) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(progress);
        }
    }

    public void refreshProgress(int position, int position2, int seekprogress, int duration) {
        note.get(position).getNoteItemList().get(position2).setProgress(seekprogress);
        note.get(position).getNoteItemList().get(position2).setDuration(duration / 1000);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}