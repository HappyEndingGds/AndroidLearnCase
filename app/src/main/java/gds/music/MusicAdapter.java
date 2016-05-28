package gds.music;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.TreeMap;

/**
 * Author:  gds
 * Time: 2016/5/20 23:09
 * E-mail: guodongshenggds@foxmail.com
 */
public class MusicAdapter extends ArrayAdapter<MusicInfo> {

    private TextView tvt;
    private boolean is =true;
    public MusicAdapter(Context context, int resource, List<MusicInfo> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MusicInfo mi = getItem(position);
        View itemView;

        ViewHolder viewHolder = new ViewHolder();
        if(convertView == null){
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.listview_item,null);
            viewHolder.tvtTitle = (TextView) itemView.findViewById(R.id.tvt_title);
            itemView.setTag(R.layout.listview_item, viewHolder);
        }
        else{
            itemView = convertView;
            viewHolder = (ViewHolder) itemView.getTag(R.layout.listview_item);
        }
        viewHolder.tvtTitle.setText(mi.getTitle()+" - "+ mi.getSinger() );

        return itemView;
    }

    private class ViewHolder {
        TextView tvtTitle;
    }
}
