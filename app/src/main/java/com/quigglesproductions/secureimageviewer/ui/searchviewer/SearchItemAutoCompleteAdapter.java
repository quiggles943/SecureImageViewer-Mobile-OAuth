package com.quigglesproductions.secureimageviewer.ui.searchviewer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedSearchItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchItemAutoCompleteAdapter extends ArrayAdapter<RoomUnifiedSearchItem> {

    private Context mContext;
    private List<RoomUnifiedSearchItem> allSearchItems = new ArrayList<>();
    private List<RoomUnifiedSearchItem> filteredSearchItems = new ArrayList<>();
    public SearchItemAutoCompleteAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        mContext = context;
    }

    @Override
    public int getCount() {
        return filteredSearchItems.size();
    }

    @Override
    public RoomUnifiedSearchItem getItem(int position) {
        return filteredSearchItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void add(@Nullable RoomUnifiedSearchItem object) {
        allSearchItems.add(object);
    }

    @Override
    public void addAll(RoomUnifiedSearchItem... items) {
        allSearchItems.addAll(Arrays.asList(items));
    }

    public void setSearchItems(List<RoomUnifiedSearchItem> searchItems) {
        allSearchItems = searchItems;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1,parent,false);
        RoomUnifiedSearchItem searchItem = getItem(position);

        TextView release = listItem.findViewById(android.R.id.text1);
        release.setText(searchItem.getName());

        return listItem;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return ((RoomUnifiedSearchItem)resultValue).getName();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                List<RoomUnifiedSearchItem> departmentsSuggestion = new ArrayList<>();
                if (constraint != null) {
                    for (RoomUnifiedSearchItem department : allSearchItems) {
                        if (department.getName().toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                            departmentsSuggestion.add(department);
                        }
                    }
                    filterResults.values = departmentsSuggestion;
                    filterResults.count = departmentsSuggestion.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredSearchItems.clear();
                if (results != null && results.count > 0) {
                    // avoids unchecked cast warning when using mDepartments.addAll((ArrayList<Department>) results.values);
                    for (Object object : (List<?>) results.values) {
                        if (object instanceof RoomUnifiedSearchItem) {
                            filteredSearchItems.add((RoomUnifiedSearchItem) object);
                        }
                    }
                    notifyDataSetChanged();
                } else if (constraint == null) {
                    // no filter, add entire original list back in
                    filteredSearchItems.addAll(allSearchItems);
                    notifyDataSetInvalidated();
                }
            }
        };
    }

}
