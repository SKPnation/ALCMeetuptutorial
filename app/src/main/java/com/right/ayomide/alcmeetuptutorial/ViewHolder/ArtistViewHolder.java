package com.right.ayomide.alcmeetuptutorial.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.right.ayomide.alcmeetuptutorial.Common.Common;
import com.right.ayomide.alcmeetuptutorial.R;

public class ArtistViewHolder extends RecyclerView.ViewHolder implements
        View.OnCreateContextMenuListener
{
    public TextView tvArtistName;
    public ImageView imageView;

    public ArtistViewHolder(@NonNull View itemView) {
        super( itemView );

        tvArtistName = itemView.findViewById(R.id.artist_name);
        imageView = itemView.findViewById(R.id.artist_image);

        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle( "Select the action" );

        contextMenu.add(0, 0, getAdapterPosition(), Common.UPDATE);
        contextMenu.add(0, 1, getAdapterPosition(), Common.DELETE);
    }
}
