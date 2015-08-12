package com.github.florent37.carpaccio.controllers.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.florent37.carpaccio.Carpaccio;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by florentchampigny on 27/07/15.
 */
public class CarpaccioRecyclerViewAdapter<T> extends RecyclerView.Adapter<Holder> {

    Carpaccio carpaccio;
    int layoutResId;
    String mappedName;
    OnItemClickListener<T> onItemClickListener;
    RecyclerViewCallback recyclerViewCallback;

    List<Integer> headerViewTypes = new ArrayList<>();
    List<Header> headers = new ArrayList<>();

    RecyclerView recyclerView;
    OnItemSwipedListener<T> onItemSwipedListener;

    public CarpaccioRecyclerViewAdapter(Carpaccio carpaccio, int layoutResId, String mappedName) {
        this.carpaccio = carpaccio;
        this.layoutResId = layoutResId;
        this.mappedName = mappedName;

        carpaccio.registerAdapter(mappedName, this);
    }

    public CarpaccioRecyclerViewAdapter(String mappedName) {
        this.mappedName = mappedName;
    }

    public void setCarpaccio(Carpaccio carpaccio) {
        this.carpaccio = carpaccio;
    }

    public void setLayoutResId(int layoutResId) {
        this.layoutResId = layoutResId;
    }

    public OnItemClickListener<T> getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener<T> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public RecyclerViewCallback getRecyclerViewCallback() {
        return recyclerViewCallback;
    }

    public void setRecyclerViewCallback(RecyclerViewCallback recyclerViewCallback) {
        this.recyclerViewCallback = recyclerViewCallback;
    }

    @Override
    public int getItemViewType(int position) {
        int itemType = -1;
        if (recyclerViewCallback != null)
            itemType = recyclerViewCallback.getItemViewType(position);
        if (itemType != -1)
            return itemType;
        else if (position < getHeaderCount()) {
            int viewType =  Integer.MIN_VALUE + position;
            headerViewTypes.add(viewType);
            return viewType;
        }
        else
            return super.getItemViewType(position);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cellView = null;
        if (recyclerViewCallback != null) {
            int layoutId = recyclerViewCallback.onCreateViewHolder(viewType);
            if (layoutId > 0)
                cellView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        }

        if(headerViewTypes.contains(viewType)){
            int layoutId = headers.get(headerViewTypes.indexOf(viewType)).layoutId;
            if (layoutId > 0)
                cellView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        }

        if (cellView == null)
            cellView = LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false);

        carpaccio.addChildViews(cellView);

        if (recyclerViewCallback != null) { //customize holder
            Holder holder = recyclerViewCallback.onCreateViewHolder(cellView, viewType);
            if (holder != null)
                return holder;
        }

        return new Holder(cellView);
    }

    public Object getItemForRow(View view, int position) {
        if (Carpaccio.IN_EDIT_MODE) {
            return new Object();
        } else if (position < getHeaderCount()) {
            return carpaccio.bindView(view, headers.get(position).mappedName);
        } else {
            return carpaccio.bindView(view, mappedName, position-getHeaderCount());
        }
    }

    public <T> T getItem(int position) {
        if (Carpaccio.IN_EDIT_MODE) {
            return (T)new Object();
        } else {
            return (T)carpaccio.getMappedList(mappedName).get(position - getHeaderCount());
        }
    }

    @Override
    public void onBindViewHolder(final Holder holder, final int position) {
        final Object mappedObject = getItemForRow(holder.itemView, position);
        holder.onItemClickListener = onItemClickListener;
        holder.position = position;
        holder.mappedObject = mappedObject;
        holder.onBind(mappedObject);

        if (recyclerViewCallback != null) {
            recyclerViewCallback.onBind(mappedObject, holder.itemView, position);
        }
    }

    @Override
    public int getItemCount() {
        int count = getHeaderCount();

        List mappedList = carpaccio.getMappedList(mappedName);
        if (mappedList != null)
            count += mappedList.size();

        return count;
    }

    public int getHeaderCount() {
        return headers.size();
    }

    public void addHeader(Header header) {
        headers.add(header);
    }

    public OnItemSwipedListener<T> getOnItemSwipedListener() {
        return onItemSwipedListener;
    }

    public void setOnItemSwipedListener(OnItemSwipedListener<T> onItemSwipedListener) {
        this.onItemSwipedListener = onItemSwipedListener;
        attachSwipeListener();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        attachSwipeListener();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    protected void attachSwipeListener(){
        if(onItemSwipedListener != null && recyclerView != null)
        {
            // init swipe to dismiss logic
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    // callback for drag-n-drop, false to skip this feature
                    return false;
                }

                @Override
                public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    if (viewHolder.getAdapterPosition() < getHeaderCount()) return 0;
                    return super.getSwipeDirs(recyclerView, viewHolder);
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    // callback for swipe to dismiss, removing item from data and adapter
                    int position = viewHolder.getAdapterPosition();
                    int positionLessHeader = viewHolder.getAdapterPosition()-getHeaderCount();

                    boolean remove = onItemSwipedListener.OnItemSwipedListener((T)getItem(viewHolder.getAdapterPosition()),viewHolder.getAdapterPosition(),(Holder)viewHolder, CarpaccioRecyclerViewAdapter.this);
                    if(remove) {
                        carpaccio.getMappedList(mappedName).remove(positionLessHeader);
                        notifyItemRemoved(position);
                    }
                    else
                        notifyItemChanged(position);
                }
            }).attachToRecyclerView(recyclerView);
        }
    }
}
