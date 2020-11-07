package com.example.dairynotes;


import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class MainFragment extends Fragment {
    private ArrayList<notesModel> myNotes = new ArrayList<>();
    private mainAdapter adapter;
    private TextView title;
    private RecyclerView recyclerView;
    private View root;
    private Toolbar tb;
    private String array;
    private FloatingActionButton newfile;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_main, container, false);
        createNotes();
        inIt();
        setListeners();
        FirebaseApp.initializeApp(getActivity());
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        FirebaseCrashlytics.getInstance().sendUnsentReports();
        ((MainActivity) getActivity()).setSupportActionBar(tb);
        title.setText(getString(R.string.app_name));
        return root;
    }

    private void setListeners() {
        newfile.setOnClickListener(v -> {
            NoteItemFragment frag = new NoteItemFragment();
            Bundle b = new Bundle();
            b.putParcelableArrayList("mynote", myNotes);
            b.putInt("position", 0);
            b.putBoolean("newfile", true);
            frag.setArguments(b);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, frag, "foodtype_Tag")
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void inIt() {
        adapter = new mainAdapter(getActivity(), myNotes);
        recyclerView = root.findViewById(R.id.recycler_note);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        recyclerView.setAdapter(adapter);
        tb = root.findViewById(R.id.toolbar);
        newfile = root.findViewById(R.id.fab);
        title = root.findViewById(R.id.title_home);
    }

    private void createNotes() {
        readFile();
        if (array != null) {
            myNotes = new Gson().fromJson(array, new TypeToken<ArrayList<notesModel>>() {
            }.getType());
        }
    }

    public void readFile() {
        try {
            FileInputStream fileInputStream = getActivity().openFileInput("Dairynotes.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuffer = new StringBuilder();

            String lines;
            while ((lines = bufferedReader.readLine()) != null) {
                stringBuffer.append(lines + "\n");
            }
            array = stringBuffer.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}