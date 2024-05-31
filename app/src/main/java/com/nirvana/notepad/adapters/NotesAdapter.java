package com.nirvana.notepad.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.nirvana.notepad.R;
import com.nirvana.notepad.activities.EditorActivity;
import com.nirvana.notepad.activities.MainActivity;
import com.nirvana.notepad.structures.Notes;

import java.util.ArrayList;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Notes> arrayList;

    public NotesAdapter(Context c, ArrayList<Notes> list){
        this.context = c;
        this.arrayList = list;
    }

    @NonNull
    @Override
    public NotesAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_layout, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final NotesAdapter.MyViewHolder holder, int position) {

        final int i = position;
        final int selectedColor = context.getResources().getColor(R.color.off_white);
        final int notSelectedColor = context.getResources().getColor(R.color.recycler_item_background);

        holder.title.setText(arrayList.get(i).getTitle());
        holder.summary.setText(arrayList.get(i).getContent());
        holder.dateTime.setText(arrayList.get(i).getDateTime());

        holder.layout.setBackgroundColor(arrayList.get(i).getSelected() ? selectedColor : notSelectedColor);

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.multiList.size() > 0) {
                    if (arrayList.get(i).getSelected()){
                        MainActivity.multiList.remove(Integer.valueOf(arrayList.get(i).getSerialNo()));
                        arrayList.get(i).setSelected(false);
                        if (MainActivity.multiList.isEmpty()){
                            ((MainActivity) context).invalidateOptionsMenu();
                        }
                    } else {
                        MainActivity.multiList.add(arrayList.get(i).getSerialNo());
                        arrayList.get(i).setSelected(true);
                    }
                    holder.layout.setBackgroundColor(arrayList.get(i).getSelected() ? selectedColor : notSelectedColor);
                    System.out.println(MainActivity.multiList);
                } else {
                    Intent intent = new Intent(context, EditorActivity.class);
                    intent.putExtra("serial_no", arrayList.get(i).getSerialNo());
                    context.startActivity(intent);
                    /*if (context instanceof MainActivity) {
                        ((MainActivity) context).finish();
                    }*/

                }
            }
        });

        holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!MainActivity.multiList.contains(arrayList.get(i).getSerialNo())) {
                    MainActivity.multiList.add(arrayList.get(i).getSerialNo());
                    arrayList.get(i).setSelected(true);
                    holder.layout.setBackgroundColor(arrayList.get(i).getSelected() ? selectedColor : notSelectedColor);
                    ((MainActivity) context).invalidateOptionsMenu();
                }
                System.out.println(MainActivity.multiList);
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, summary, dateTime;
        LinearLayout layout;
        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.recycler_title);
            summary = itemView.findViewById(R.id.recycler_content_summary);
            dateTime = itemView.findViewById(R.id.recycler_date_time);
            layout = itemView.findViewById(R.id.linear_layout);
        }
    }
}
