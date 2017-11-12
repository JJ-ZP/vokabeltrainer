package it.tfobz.jj_zp.vokabeltrainer;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class LernkarteiActivity extends AppCompatActivity {

    private VokabeltrainerDB vokabeltrainerDB;
    private RecyclerView recyclerView;
    private CardAdapter lernkarteiAdapter;
    private int lernkarteinummer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lernkartei);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        vokabeltrainerDB = VokabeltrainerDB.getInstance(this);

        recyclerView = findViewById(R.id.listCards);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        lernkarteinummer = getIntent().getIntExtra("it.tfobz.jj_zp.vokabeltrainer.KarteiId", 0);
        lernkarteiAdapter = new CardAdapter(vokabeltrainerDB, lernkarteinummer);
        recyclerView.setAdapter(lernkarteiAdapter);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {

                AlertDialog.Builder builder = new AlertDialog.Builder(LernkarteiActivity.this);
                builder.setTitle("Karte wirklich löschen?");
                builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton("Löschen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int pos = viewHolder.getAdapterPosition();
                        vokabeltrainerDB.loeschenKarte(vokabeltrainerDB.getAllKarten(lernkarteinummer).get(pos).getNummer());
                        lernkarteiAdapter.notifyDataSetChanged();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                lernkarteiAdapter.notifyDataSetChanged();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void startAddCardDialog(final View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View v = View.inflate(view.getContext(), R.layout.card_dialog, null);
        builder.setView(v);
        builder.setTitle("Karte hinzufügen");
        builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setPositiveButton("Hinzufügen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditText vorneTE = v.findViewById(R.id.cardVorne);
                EditText hintenTE = v.findViewById(R.id.cardHinten);
                Switch grossKleinTgl = v.findViewById(R.id.cardGrossKleinTgl);

                Karte karte = new Karte(-1, vorneTE.getText().toString(), hintenTE.getText().toString(),
                        false, grossKleinTgl.isChecked());
                karte.validiere();
                if(karte.getFehler() == null){
                    VokabeltrainerDB vokabeltrainerDB = VokabeltrainerDB.getInstance(view.getContext());
                    if(vokabeltrainerDB.hinzufuegenKarte(lernkarteinummer, karte) == 0){
                        lernkarteiAdapter.notifyDataSetChanged();
                        Toast.makeText(view.getContext(), "Karte hinzugefügt", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(view.getContext(), "Fehler beim Speichern", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(view.getContext(), "Ungültige Angabe", Toast.LENGTH_SHORT).show();
                }
                Log.i("LLOG", "Items: " +lernkarteiAdapter.getItemCount());
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
