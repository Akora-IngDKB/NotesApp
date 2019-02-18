package com.akoraingdkb.notesapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.ArrayList;
import java.util.List;

class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ViewHolder> implements Constants, Filterable {
    private Context mContext;
    private List<Note> mData;
    private List<Note> mDataFiltered;
    private static String noteTitle = null;

    RecycleAdapter(Context context, List<Note> Data) {
        this.mContext = context;
        this.mData = Data;
        this.mDataFiltered = Data;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_row, parent, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view with desired data (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Note currentNote = mDataFiltered.get(position);

        // Set the title to the text view
        holder.textView.setText(currentNote.getTitle());

        // Create a text drawable with a random color
        ColorGenerator generator = ColorGenerator.MATERIAL;
        TextDrawable textDrawable = TextDrawable.builder().buildRound(currentNote.getFirstLetter(), generator.getColor(currentNote.getTitle()));

        // Use the text drawable for the image view
        holder.imageView.setImageDrawable(textDrawable);

    }

    void deleteItem(int position) {
        mData.remove(position);
        notifyItemRemoved(position);
    }

    void restoreItem(int position, Note note) {
        mData.add(position, note);
        notifyItemInserted(position);
    }

    @Override
    public int getItemCount() {
        return mDataFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();

                if (charString.isEmpty())
                    mDataFiltered = mData;
                else {
                    List<Note> filteredList = new ArrayList<>();
                    for (Note row : mData) {
                        // Name match condition.
                        if (row.getTitle().toLowerCase().contains(charString.toLowerCase()))
                            filteredList.add(row);
                    }

                    mDataFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mDataFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                //noinspection unchecked
                mDataFiltered = (ArrayList<Note>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    // Provide a reference to the view of each data item
    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        ViewHolder(View itemView) {
            super(itemView);

            // Obtain references to all views on the custom row
            imageView = itemView.findViewById(R.id.recycle_image_view);
            textView = itemView.findViewById(R.id.recycle_text_view);

            //
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //
                    noteTitle = mDataFiltered.get(getAdapterPosition()).getTitle();
                    Intent editNoteIntent = new Intent(mContext, EditNoteActivity.class);
                    editNoteIntent.putExtra(SELECTED_NOTE_TITLE, noteTitle);
                    mContext.startActivity(editNoteIntent);
                }
            });
        }
    }

}
