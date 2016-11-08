package com.andreassavva.expensemanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class TabFragment1 extends Fragment {

    // Den här är den första tab:en "IOU", som visar alla IOU:ar.

    private static final String TAG = "AKS";
    private static final String FILENAME = "contact_list.iou";
    public static final int ADD_IOU_REQUEST = 1;
    public static final int EDIT_IOU_REQUEST = 2;

    private Context context = getContext();
    private List<Contact> contactList;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipreRefreshLayout;
    private RecyclerView.LayoutManager mLayoutManager;
    private ContactViewAdapter mAdapter;
    private ImageView emptyImg;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.tab_fragment_1, container, false);

        contactList = new ArrayList<>();

        // Först laddar contactList från Internal Storage.
        FileInputStream fis;
        try {
            fis = getContext().openFileInput(FILENAME);
            ObjectInputStream oi = new ObjectInputStream(fis);
            contactList = (List<Contact>) oi.readObject();
            oi.close();
        } catch (IOException e) {
            Log.e("InternalStorage", e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Skapar FAB.
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_add);
        fab.setOnClickListener(null);
        fab.setOnClickListener(fabOnClickListener);

        // Sätter upp RecyclerView i Tab1.
        mSwipreRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.listRecyclerView);
        emptyImg = (ImageView) rootView.findViewById(R.id.emptyImg);
        // Adderar en divider.
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mSwipreRefreshLayout.setOnRefreshListener(onRefreshListener);

        // Skapar en ContactViewAdapter
        mAdapter = new ContactViewAdapter(contactList, (AppCompatActivity) getActivity(), getContext());
        mRecyclerView.setAdapter(mAdapter);

        // Visar en bild om contactList är tom.
        if (contactList.isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            emptyImg.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            emptyImg.setVisibility(View.GONE);
        }

        return rootView;
    }

    private SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            //När man refreshar. Laddar contactList från minnet.
            FileInputStream fis;
            try {
                fis = getContext().openFileInput(FILENAME);
                ObjectInputStream oi = new ObjectInputStream(fis);
                contactList = (List<Contact>) oi.readObject();
                oi.close();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Visar en bild om contactList är tom
            mAdapter.notifyDataSetChanged();
            if (contactList.isEmpty()) {
                mRecyclerView.setVisibility(View.GONE);
                emptyImg.setVisibility(View.VISIBLE);
            } else {
                mRecyclerView.setVisibility(View.VISIBLE);
                emptyImg.setVisibility(View.GONE);
            }
            mSwipreRefreshLayout.setRefreshing(false);
        }
    };

    private FloatingActionButton.OnClickListener fabOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Startar aktivitet IouAdd och väntar på resultat.
            Intent intent = new Intent(getContext(), IouAdd.class);
            startActivityForResult(intent, ADD_IOU_REQUEST);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // När man får tillbaka ett resultat (här är det endast IouAdd som skickar tillbaka resultat.
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_IOU_REQUEST) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                long contactId = data.getLongExtra("contactId", 0);
                String moneyOwed = data.getStringExtra("moneyOwed");

                // Metoden som kollar om kontakten redan finns, och tar bort IOU:n.
                for (int i = contactList.size() - 1; i >= 0; i--) {
                    if (contactList.get(i).getId() == contactId) {
                        int oldMoneyOwed = Integer.parseInt(contactList.get(i).getMoneyOwed());
                        int newMoneyOwed = Integer.parseInt(moneyOwed);
                        moneyOwed = String.valueOf(oldMoneyOwed + newMoneyOwed);
                        contactList.remove(i);
                    }
                }

                // Adderar den nya IOU i början av listan.
                if (!moneyOwed.equals("0")) {
                    Contact contact = new Contact(getContext(), contactId, moneyOwed);
                    contactList.add(0, contact);
                }

                mRecyclerView.setVisibility(View.VISIBLE);
                emptyImg.setVisibility(View.GONE);

                mAdapter.notifyDataSetChanged();

                for (int i = 0; i < contactList.size(); i++) {
                    contactList.get(i).setIsSelected(false);
                }

                // Sparar listan i Internal storage.
                FileOutputStream fos = null;
                ObjectOutputStream out = null;
                try {
                    fos = getActivity().openFileOutput(FILENAME, getActivity().MODE_PRIVATE);
                    out = new ObjectOutputStream(fos);
                    out.writeObject(contactList);
                    out.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onResume() {
        // När man återkommer till appen.
        // Laddar listan från minnet.
        FileInputStream fis;
        try {
            fis = getContext().openFileInput(FILENAME);
            ObjectInputStream oi = new ObjectInputStream(fis);
            contactList = (List<Contact>) oi.readObject();
            oi.close();
        } catch (FileNotFoundException e) {
            Log.e("InternalStorage", e.getMessage());
        } catch (IOException e) {
            Log.e("InternalStorage", e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Skapar ny adapter och sätter den i RecyclerView
        mAdapter = new ContactViewAdapter(contactList, (AppCompatActivity) getActivity(), getContext());
        mRecyclerView.setAdapter(mAdapter);

        // Visar bild om listan är tom.
        if (contactList.isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            emptyImg.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            emptyImg.setVisibility(View.GONE);
        }
        super.onResume();
    }
}
