package com.iiitd.esya.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by darkryder on 27/6/15.
 * As of now, this is a temporary class to hold the category/event/details data
 */
public class DataHolder {
    private static final String LOG_TAG = DataHolder.class.getSimpleName();

    public static final String ELIGIBILITY_RESPONSE = "eligibilty";
    public static final String JUDGING_RESPONSE =  "judging";
    public static final String PRIZES_RESPONSE =  "prizes";
    public static final String RULES_RESPONSE =  "rules";
    public static final String VENUE_RESPONSE =  "venue";
    public static final String DESCRIPTION_RESPONSE =  "description";
    public static final String TEAM_SIZE_RESPONSE =  "team_size";
    public static final String CONTACT_RESPONSE =  "contact";
    public static final String REGISTERED_RESPONSE = "registered";
    public static final String TEAM_EVENT_RESPONSE = "team_event";
    public static final String TEAM_ID_RESPONSE = "team_id";
    public static final String UPDATED_AT_RESPONSE = "updated_at";
    public static final String EVENT_DATE_TIME_RESPONSE = "event_date_time";

    public static final String ELIGIBILITY_DEFAULT = "No specific eligibility criteria.";
    public static final String JUDGING_DEFAULT =  "Judging criteria has not been decided yet. Stay tuned!";
    public static final String PRIZES_DEFAULT =  "Prizes have not been announced yet. Stay tuned, though!";
    public static final String RULES_DEFAULT =  "Rules have not been decided yet.";
    public static final String VENUE_DEFAULT =  "IIIT - D";
    public static final String DESCRIPTION_DEFAULT =  "Description of the event";
    public static final int TEAM_SIZE_DEFAULT =  0;
    public static final String CONTACT_DEFAULT =  "events.esya@iiitd.ac.in";
    public static final int REGISTERED_DEFAULT = 0;
    public static final boolean TEAM_EVENT_DEFAULT = false;
    public static final int TEAM_ID_DEFAULT = 0;
    public static final Date UPDATED_AT_DEFAULT = new Date();
    public static final Date EVENT_DATE_TIME_DEFAULT = new Date();

    public static final HashMap<Category, ArrayList<Event>> CATEGORY_TO_EVENTS = new HashMap<>();

    public static boolean initialised = false;

    public static void init(Context context)
    {
        if (initialised) return;

        for(Category c: Category.values()){
            CATEGORY_TO_EVENTS.put(c, new ArrayList<Event>());
        }

        InitialDataFetcher task = new InitialDataFetcher(context);
        task.execute();
    }

    public static HashMap<Integer, Event> EVENTS = new HashMap<>();
}

class InitialDataFetcher extends FetchAllEventsTask
{
    Context context;
    public InitialDataFetcher(Context context)
    {
        super(context);
        this.context = context;
    }

    public static final String ERROR_TOAST = "No network connection";
    public static final String LOG_TAG = InitialDataFetcher.class.getSimpleName();

    @Override
    protected void onPostExecute(Event[] events) {
        if (events == null){
            Log.e(LOG_TAG, "Could not fetch initial Data. No connection");
            return;
        }
        Log.v(LOG_TAG, "Fetched all events: " + Arrays.deepToString(events));
        if (DataHolder.initialised == true) return;
        for(Event ev: events){
            for(Category category: ev.categories)
            {
                DataHolder.CATEGORY_TO_EVENTS.get(category).add(ev);
            }
            DataHolder.CATEGORY_TO_EVENTS.get(Category.ALL).add(ev);
            DataHolder.EVENTS.put(ev.id, ev);
        };
        DataHolder.initialised = true;

        DBHelper db = new DBHelper(context);
        boolean check;
        for(Event e: events) {
            check = db.insertEvent(e);
            if(check) {
                Log.v(LOG_TAG, "Inserted Event " + e.toString() + "to database");
            } else {
                Log.e(LOG_TAG, "Could not insert Event " + e.toString() + "to database");
            }

            Event ev1 = db.getEvent(e.id);
            if(ev1.id == e.id) {
                Log.v(LOG_TAG, "Database insertion successful");
            } else {
                Log.e(LOG_TAG, "NOT INSERTED PROPERLY IN DB");
            }
        }

        InitialImagesFetcher task = new InitialImagesFetcher(context);
        String[] event_image_urls = new String[events.length];
        for(int i = 0; i < events.length; i++)
        {
            event_image_urls[i] = events[i].image_url;
        }
        task.execute(event_image_urls);

    }
}

class InitialImagesFetcher extends FetchImagesTask
{
    public InitialImagesFetcher(Context context)
    {
        super(context);
    }

    public static final String LOG_TAG =InitialImagesFetcher.class.getSimpleName();

    @Override
    protected void onPostExecute(Bitmap[] bitmaps) {
        Log.v(LOG_TAG, "FETCHED ALL IMAGES");
    }
}