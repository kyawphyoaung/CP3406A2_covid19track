package com.jcueduau.covid19tracker;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    TextView totalcase, activecase, recovered, deaths, casetdy, deathstdy, update;
    SwipeRefreshLayout mySwipeRefreshLayout;
    Document doc, germanDoc;
    String url = "https://www.worldometers.info/coronavirus/";
    Element countriesTable, row, germanTable;
    Elements countriesRows, cols, germanRows;
    Iterator<Element> rowIterator;
    ArrayList<CountryLine> allCountriesResults, FilteredArrList;
    int colNumCountry, colNumCases, colNumRecovered, colNumDeaths, colNumActive, colNumNewCases, colNumNewDeaths;
    String tmpCountry, tmpCases, tmpRecovered, tmpDeaths, tmpPercentage, germanResults, tmpNewCases, tmpNewDeaths;
    DecimalFormat generalDecimalFormat;
    Calendar myCalender;
    SimpleDateFormat myFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        totalcase =(TextView)findViewById(R.id.totalcasetxt);
        activecase =(TextView)findViewById(R.id.activecasetxt);
        recovered =(TextView)findViewById(R.id.recoveredtxt);
        deaths =(TextView)findViewById(R.id.deathstxt);
        casetdy =(TextView)findViewById(R.id.casetdytxt);
        deathstdy=(TextView)findViewById(R.id.deathstdytxt);
        update =(TextView)findViewById(R.id.updatetxt);
        allCountriesResults = new ArrayList<CountryLine>();
        mySwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.coronaMainSwipeRefresh);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshData();
                    }
                }
        );
        refreshData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }



    void refreshData() {
        mySwipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    doc = null; // Fetches the HTML document
                    doc = Jsoup.connect(url).timeout(10000).get();
                    // table id main_table_countries
                    countriesTable = doc.select("table").get(0);
                    countriesRows = countriesTable.select("tr");
                    //Log.e("TITLE", elementCases.text());
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // get countries
                            rowIterator = countriesRows.iterator();
                            allCountriesResults = new ArrayList<CountryLine>();

                            // read table header and find correct column number for each category
                            row = rowIterator.next();
                            cols = row.select("th");
                            Log.e("COLS: ", cols.text());
                            Log.e("row: ",row.text());
                            if (cols.get(0).text().contains("Country")) {
                                for(int i=1; i < cols.size(); i++){
                                    if (cols.get(i).text().contains("Total") && cols.get(i).text().contains("Cases"))
                                    {colNumCases = i; Log.e("Cases: ", cols.get(i).text());}
                                    else if (cols.get(i).text().contains("Total") && cols.get(i).text().contains("Recovered"))
                                    {colNumRecovered = i; Log.e("Recovered: ", cols.get(i).text());}
                                    else if (cols.get(i).text().contains("Total") && cols.get(i).text().contains("Deaths"))
                                    {colNumDeaths = i; Log.e("Deaths: ", cols.get(i).text());}
                                    else if (cols.get(i).text().contains("Active") && cols.get(i).text().contains("Cases"))
                                    {colNumActive = i; Log.e("Active: ", cols.get(i).text());}
                                    else if (cols.get(i).text().contains("New") && cols.get(i).text().contains("Cases"))
                                    {colNumNewCases = i; Log.e("NewCases: ", cols.get(i).text());}
                                    else if (cols.get(i).text().contains("New") && cols.get(i).text().contains("Deaths"))
                                    {colNumNewDeaths = i; Log.e("NewDeaths: ", cols.get(i).text());}
                                }
                            }

                            while (rowIterator.hasNext()) {
                                row = rowIterator.next();
                                cols = row.select("td");

                                if (cols.get(0).text().contains("World")) {
                                    totalcase.setText(cols.get(colNumCases).text());
                                    recovered.setText(cols.get(colNumRecovered).text());
                                    deaths.setText(cols.get(colNumDeaths).text());

                                    if (cols.get(colNumActive).hasText()) {activecase.setText(cols.get(colNumActive).text());}
                                    else {activecase.setText("0");}
                                    if (cols.get(colNumNewCases).hasText()) {casetdy.setText(cols.get(colNumNewCases).text());}
                                    else {casetdy.setText("0");}
                                    if (cols.get(colNumNewDeaths).hasText()) {deathstdy.setText(cols.get(colNumNewDeaths).text());}
                                    else {deathstdy.setText("0");}
                                    continue;
                                } else if (
                                        cols.get(0).text().contains("Total") ||
                                                cols.get(0).text().contains("Europe") ||
                                                cols.get(0).text().contains("North America") ||
                                                cols.get(0).text().contains("Asia") ||
                                                cols.get(0).text().contains("South America") ||
                                                cols.get(0).text().contains("Africa") ||
                                                cols.get(0).text().contains("Oceania")
                                ) {
                                    continue;
                                }

                                if (cols.get(colNumCountry).hasText()) {tmpCountry = cols.get(0).text();}
                                else {tmpCountry = "NA";}

                                if (cols.get(colNumCases).hasText()) {tmpCases = cols.get(colNumCases).text();}
                                else {tmpCases = "0";}

                                if (cols.get(colNumRecovered).hasText()){
                                    if(!cols.get(colNumRecovered).text().contains("N/A")) {
                                        tmpRecovered = cols.get(colNumRecovered).text();
                                        Log.e("recovertmplog",tmpRecovered);;
                                    }
                                    else {tmpRecovered = "NA";}
                                }
                                else {tmpRecovered = "0";}

                                if(cols.get(colNumDeaths).hasText()) {
                                    if(!cols.get(colNumDeaths).text().contains("N/A")) {
                                        tmpDeaths = cols.get(colNumDeaths).text();
                                    }
                                    else {tmpDeaths = "NA";}
                                }
                                else {tmpDeaths = "0";}

                                if (cols.get(colNumNewCases).hasText()) {tmpNewCases = cols.get(colNumNewCases).text();}
                                else {tmpNewCases = "0";}

                                if (cols.get(colNumNewDeaths).hasText()) {tmpNewDeaths = cols.get(colNumNewDeaths).text();}
                                else {tmpNewDeaths = "0";}

                                allCountriesResults.add(new CountryLine(tmpCountry, tmpCases, tmpNewCases, tmpRecovered, tmpDeaths, tmpNewDeaths));
                            }

                            myCalender = Calendar.getInstance();
                            System.out.println(" Current time is : " + myCalender.getTime());
                            update.setText("Last updated: " + myCalender.getTime());
//                            update.setText("Last updated: " + myFormat.format(Calendar.getInstance().getTime()));
                        }
                    });
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Network Connection Error!",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
                finally {
                    doc = null;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mySwipeRefreshLayout.setRefreshing(false);
                    }});
            }
        }).start();
    }
}
