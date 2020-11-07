package com.example.dairynotes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class noteAdapter extends RecyclerView.Adapter<noteAdapter.ViewHolder> {
    Context context;
    ArrayList<customType> list;
    onItemClick onItemClick;
    String type;
    updateSeek updateSeek;


    public noteAdapter(Context context, ArrayList<customType> list, onItemClick onItemClick, updateSeek updateSeek) {
        this.context = context;
        this.list = list;
        this.onItemClick = onItemClick;
        this.updateSeek = updateSeek;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.note_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (list.size() > 0) {
            if (list.get(position).getType().equalsIgnoreCase("string")) {
                holder.imagelay.setVisibility(View.GONE);
                holder.audiolay.setVisibility(View.GONE);
                type = "string";
                holder.note.setText((String) list.get(position).getMyObject());
            } else if (list.get(position).getType().equalsIgnoreCase("image")) {
                holder.imagelay.setVisibility(View.VISIBLE);
                holder.audiolay.setVisibility(View.GONE);
                holder.note.setVisibility(View.GONE);
                type = "image";
                holder.image.setImageBitmap(StringToBitMap((String) list.get(position).getMyObject()));
                holder.imagetitle.setText(list.get(position).getTitle());

            } else if (list.get(position).getType().equalsIgnoreCase("audio")) {
                holder.imagelay.setVisibility(View.GONE);
                holder.audiolay.setVisibility(View.VISIBLE);
                holder.note.setVisibility(View.GONE);
                holder.audiotitle.setText(list.get(position).getTitle());
                type = "audio";
                if (list.get(position).getDuration() != 0) {
                    holder.seekBar.setProgress(list.get(position).getProgress());
                    holder.seekBar.setMax(list.get(position).getDuration());

                    holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser) {
                                updateSeek.update(progress * 1000);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                }

            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View imagelay, audiolay;
        ImageView image, pause;
        TextView imagetitle, note, audiotime, audiotitle;
        SeekBar seekBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imagelay = itemView.findViewById(R.id.imagelay);
            audiolay = itemView.findViewById(R.id.audiolay);
            image = itemView.findViewById(R.id.image);
            seekBar = itemView.findViewById(R.id.seekbar);
            pause = itemView.findViewById(R.id.pauseimage);
            imagetitle = itemView.findViewById(R.id.imagetitle);
            note = itemView.findViewById(R.id.note);
            //  audiotime = itemView.findViewById(R.id.audiotime);
            audiotitle = itemView.findViewById(R.id.audiotitle);
            pause.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemClick.OnClickListener(list.get(getAdapterPosition()), getAdapterPosition(), type);
        }
    }

    public interface onItemClick {
        void OnClickListener(customType n, int position, String type);
    }

    public interface updateSeek {
        void update(int progress);
    }

    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

}
