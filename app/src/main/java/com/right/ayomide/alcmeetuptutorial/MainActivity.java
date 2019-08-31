package com.right.ayomide.alcmeetuptutorial;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.right.ayomide.alcmeetuptutorial.Common.Common;
import com.right.ayomide.alcmeetuptutorial.Model.Artists;
import com.right.ayomide.alcmeetuptutorial.ViewHolder.ArtistViewHolder;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase db;
    DatabaseReference artists;
    FirebaseStorage storage;
    StorageReference storageReference;

    FirebaseRecyclerAdapter<Artists, ArtistViewHolder> adapter;

    Artists newArtist;

    //Add new menu layout
    MaterialEditText edtName;
    Button btnSelect, btnUpload;

    //View
    RecyclerView recycler_artists;
    RecyclerView.LayoutManager layoutManager;

    Uri saveUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        toolbar.setTitle( "Top 5 artists in Nigeria" );
        setSupportActionBar( toolbar );

        db = FirebaseDatabase.getInstance();
        artists = db.getReference("Artists");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        FloatingActionButton fab = (FloatingActionButton) findViewById( R.id.fab );
        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        } );

        //Load menu
        recycler_artists = (RecyclerView) findViewById(R.id.recycler_artists);
        recycler_artists.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycler_artists.setLayoutManager(layoutManager);

        loadArtists();
    }

    private void loadArtists()
    {
        adapter = new FirebaseRecyclerAdapter<Artists, ArtistViewHolder>(
                Artists.class, R.layout.artist_layout, ArtistViewHolder.class, artists) {
            @Override
            protected void populateViewHolder(ArtistViewHolder viewHolder, Artists model, int position) {
                viewHolder.tvArtistName.setText(model.getName());
                Picasso.with(MainActivity.this).load(model.getImage()).into(viewHolder.imageView);
            }
        };

        adapter.notifyDataSetChanged(); //refresh data if changed
        //Set Adapter
        recycler_artists.setAdapter(adapter);
    }

    private void showDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Add new Artist");
        alertDialog.setMessage("Put in full name");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_artist_layout = inflater.inflate(R.layout.add_new_artist_layout, null);

        edtName = add_artist_layout.findViewById(R.id.etName);
        btnSelect = add_artist_layout.findViewById(R.id.btnSelect);
        btnUpload = add_artist_layout.findViewById(R.id.btnUpload);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(); //let user select image from gallery and save Uri of this image
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        alertDialog.setView(add_artist_layout);

        //set button
        alertDialog.setPositiveButton( "YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                //Here create new category
                if(newArtist != null)
                {
                    artists.push().setValue(newArtist);
                }
            }
        });

        alertDialog.setNegativeButton( "NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
    }

    private void chooseImage()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select A Picture"), Common.IMAGE_REQUEST);
    }

    private void uploadImage()
    {
        if(saveUri != null)
        {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/"+imageName);
            imageFolder.putFile(saveUri).
                    addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Uploaded!!!", Toast.LENGTH_SHORT).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener( new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            newArtist = new Artists(edtName.getText().toString(), uri.toString());
                        }
                    });
                }
            } ).addOnFailureListener( new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDialog.dismiss();
                    Toast.makeText( MainActivity.this, ""+e.getMessage(), Toast.LENGTH_LONG ).show();
                }
            } ).addOnProgressListener( new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    mDialog.setMessage( "Uploaded "+progress+"%" );
                }
            } );
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if(item.getTitle().equals(Common.UPDATE))
        {
            //showEditDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        }
        else if(item.getTitle().equals(Common.DELETE))
        {
            //deleteCategory(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.menu_main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected( item );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );

        if(requestCode == Common.IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null)
        {
            saveUri = data.getData();
            btnSelect.setText("Image Selected");
        }
    }
}
