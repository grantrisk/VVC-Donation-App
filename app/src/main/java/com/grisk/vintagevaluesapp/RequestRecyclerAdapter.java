package com.grisk.vintagevaluesapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class RequestRecyclerAdapter extends FirestoreRecyclerAdapter<Request, RequestRecyclerAdapter.RequestViewHolder> {


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private final SimpleDateFormat format = new SimpleDateFormat("MM-dd-yy", Locale.US);
    private final OnItemClickListener listener;

    RequestRecyclerAdapter(FirestoreRecyclerOptions<Request> options, OnItemClickListener listener) {
        super(options);
        this.listener = listener;
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        final CardView view;
        final TextView firstName;
        final TextView lastName;
        final TextView createdOn;
        final TextView bags;
        final TextView description;

        RequestViewHolder(CardView v) {
            super(v);
            view = v;
            firstName = v.findViewById(R.id.item_first_name);
            lastName = v.findViewById(R.id.item_last_name);
            createdOn = v.findViewById(R.id.item_created_on);
            bags = v.findViewById(R.id.bag_requests);
            description = v.findViewById(R.id.location_description);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RequestViewHolder holder, @NonNull int position, @NonNull final Request request) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.firstName.setText(request.getFirst());
        holder.lastName.setText(request.getLast());
        holder.createdOn.setText(holder.view.getContext()
                .getString(R.string.created_on, format.format(request.getCreatedTime())));
        holder.bags.setText(request.getBags());
        holder.description.setText(request.getPickupDescription());
        if (listener != null) {
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(holder.getAbsoluteAdapterPosition());
                }
            });
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RequestViewHolder onCreateViewHolder(ViewGroup parent,
                                                int viewType) {
        // create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.request_list_item, parent, false);

        return new RequestViewHolder(v);
    }
}
