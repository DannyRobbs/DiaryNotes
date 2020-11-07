package com.example.dairynotes;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class mainAdapter extends RecyclerView.Adapter<mainAdapter.ViewHolder> {
    Context context;
    ArrayList<notesModel> list;

    public mainAdapter(Context context, ArrayList<notesModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.main_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(list.get(position).getTitle());
        holder.title.setOnClickListener(v -> openNewFragment(position));
        holder.img.setOnClickListener(v -> openNewFragment(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView img;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            img = itemView.findViewById(R.id.img);
        }
    }

    private void openNewFragment(int position) {
        NoteItemFragment frag = new NoteItemFragment();
        Bundle b = new Bundle();
        b.putParcelableArrayList("mynote", list);
        b.putInt("position", position);
        b.putBoolean("newfile", false);
        frag.setArguments(b);

        ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, frag, "foodtype_Tag")
                .addToBackStack(null)
                .commit();
    }


}
