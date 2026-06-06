package com.elderwatch.client.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elderwatch.client.R;
import com.elderwatch.client.models.OverviewCard;
import com.github.MakMoinee.library.interfaces.DefaultEventListener;

import java.util.List;

public class OverviewAdapter extends RecyclerView.Adapter<OverviewAdapter.ViewHolder> {

    Context mContext;
    List<OverviewCard> list;

    DefaultEventListener listener;

    public OverviewAdapter(Context mContext, List<OverviewCard> list, DefaultEventListener listener) {
        this.mContext = mContext;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OverviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.item_overview, null, false);
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull OverviewAdapter.ViewHolder holder, int position) {
        OverviewCard card = list.get(position);
        holder.txtLabel.setText(card.getLabel());
        holder.txtCount.setText(card.getCount());
        holder.imgLogo.setImageResource(card.getImageResource());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtCount, txtLabel;
        ImageView imgLogo;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCount = itemView.findViewById(R.id.txtCount);
            txtLabel = itemView.findViewById(R.id.txtCardLabel);
            imgLogo = itemView.findViewById(R.id.imgLogo);
        }
    }
}
