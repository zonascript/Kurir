package id.co.kurindo.kurindo.adapter;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import id.co.kurindo.kurindo.R;
import id.co.kurindo.kurindo.app.AppConfig;
import id.co.kurindo.kurindo.model.Packet;

/**
 * Created by DwiM on 11/9/2016.
 */
public class MonitorPacketAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<Packet> data = new ArrayList<>();
    private String[] bgColors;
    private OnItemClickListener itemClickListener;

    public MonitorPacketAdapter(Context context, List<Packet> data, OnItemClickListener mOnItemClickListener) {
        this.context = context;
        this.data = data;
        this.itemClickListener = mOnItemClickListener;
        bgColors = context.getResources().getStringArray(R.array.list_bg);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_monitor_packet, parent, false);
        viewHolder = new MyItemHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        Packet packet = data.get(position);
        ((MyItemHolder) holder).awbText.setText(packet.getResi());
        ((MyItemHolder) holder).namaPengirimText.setText(packet.getNamaPengirim());
        ((MyItemHolder) holder).teleponPengirimText.setText(packet.getTeleponPengirim());
        ((MyItemHolder) holder).kotaAsalText.setText("dari "+packet.getKotaPengirimText());
        ((MyItemHolder) holder).kotaTujuanText.setText("ke "+packet.getKotaPenerimaText());
        //((MyItemHolder) holder).serviceCodeText.setText(packet.getServiceCode());
        ((MyItemHolder) holder).picText.setText(packet.getKurir() == null? "": "PIC: "+packet.getKurir().getFirstname() +" "+packet.getKurir().getLastname());
        ((MyItemHolder) holder).statusText.setText(packet.getStatusText());
        ((MyItemHolder) holder).createdText.setText(packet.getCreatedDate());
        //String color = bgColors[position % bgColors.length];
        if(packet.getServiceCode().equalsIgnoreCase("NDS")){
            ((MyItemHolder) holder).item_service.setImageResource(R.drawable.icon_nds);
        }else {
            ((MyItemHolder) holder).item_service.setImageResource(R.drawable.icon_sds);
        }
        if(packet.getOrder() != null){
            if(packet.getOrder().getType().equalsIgnoreCase(AppConfig.KEY_DOWASH)){
                ((MyItemHolder) holder).item_service_type.setImageResource(R.drawable.do_wash_icon);
            }else if(packet.getOrder().getType().equalsIgnoreCase(AppConfig.KEY_DOJEK)){
                ((MyItemHolder) holder).item_service_type.setImageResource(R.drawable.do_jek_icon);
            }else{
                ((MyItemHolder) holder).item_service_type.setImageResource(R.drawable.do_send_icon);
            }

        }
        if(packet.getStatus().equalsIgnoreCase(AppConfig.KEY_KUR100)) {
            ((MyItemHolder) holder).kur101Btn.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur101Btn.setImageResource(R.drawable.booking_order);
            ((MyItemHolder) holder).kur101Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onPickButtonClick(v, position, AppConfig.KEY_KUR101);
                }
            });

            ((MyItemHolder) holder).picText.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur500Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur400Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur350Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur300Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur310Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur200Btn.setVisibility(View.GONE);

        }else if(packet.getStatus().equalsIgnoreCase(AppConfig.KEY_KUR101)){
            ((MyItemHolder) holder).kur200Btn.setImageResource(R.drawable.accept_booking_icon);
            ((MyItemHolder) holder).kur200Btn.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur200Btn.setEnabled(true);
            ((MyItemHolder) holder).kur200Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onPickButtonClick(v, position, AppConfig.KEY_KUR200);
                }
            });

            ((MyItemHolder) holder).kur100Btn.setImageResource(R.drawable.reject_booking_icon);
            ((MyItemHolder) holder).kur100Btn.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur100Btn.setEnabled(true);
            ((MyItemHolder) holder).kur100Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onPickButtonClick(v, position, AppConfig.KEY_KUR100);
                }
            });

            ((MyItemHolder) holder).picText.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur101Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur300Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur310Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur350Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur400Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur500Btn.setVisibility(View.GONE);

        }else if(packet.getStatus().equalsIgnoreCase(AppConfig.KEY_KUR200)){
            ((MyItemHolder) holder).kur300Btn.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur300Btn.setEnabled(true);
            ((MyItemHolder) holder).kur300Btn.setImageResource(R.drawable.status01_1_icon);
            ((MyItemHolder) holder).kur300Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onPickButtonClick(v, position, AppConfig.KEY_KUR300);
                }
            });

            ((MyItemHolder) holder).picText.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur100Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur101Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur200Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur350Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur400Btn.setVisibility(View.GONE);

            ((MyItemHolder) holder).kur310Btn.setImageResource(R.drawable.status03_0_icon);
            ((MyItemHolder) holder).kur310Btn.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur310Btn.setEnabled(false);

            ((MyItemHolder) holder).kur500Btn.setImageResource(R.drawable.status04_0_icon);
            ((MyItemHolder) holder).kur500Btn.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur500Btn.setEnabled(false);
        }else if(packet.getStatus().equalsIgnoreCase(AppConfig.KEY_KUR300)){
            ((MyItemHolder) holder).kur310Btn.setImageResource(R.drawable.status03_1_icon);
            ((MyItemHolder) holder).kur310Btn.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur310Btn.setEnabled(true);
            ((MyItemHolder) holder).kur310Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onPickButtonClick(v, position, AppConfig.KEY_KUR310);
                }
            });

            ((MyItemHolder) holder).picText.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur100Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur101Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur200Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur350Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur400Btn.setVisibility(View.GONE);

            ((MyItemHolder) holder).kur300Btn.setImageResource(R.drawable.status01_2_icon);
            ((MyItemHolder) holder).kur300Btn.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur300Btn.setEnabled(false);

            ((MyItemHolder) holder).kur500Btn.setImageResource(R.drawable.status04_0_icon);
            ((MyItemHolder) holder).kur500Btn.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur500Btn.setEnabled(false);

        }else if(packet.getStatus().equalsIgnoreCase(AppConfig.KEY_KUR310) || packet.getStatus().equalsIgnoreCase(AppConfig.KEY_KUR350)){
            ((MyItemHolder) holder).kur400Btn.setImageResource(R.drawable.status05_1_icon);
            ((MyItemHolder) holder).kur400Btn.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur400Btn.setEnabled(true);
            ((MyItemHolder) holder).kur400Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onPickButtonClick(v, position, AppConfig.KEY_KUR400);
                }
            });

            ((MyItemHolder) holder).kur500Btn.setImageResource(R.drawable.status04_1_icon);
            ((MyItemHolder) holder).kur500Btn.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur500Btn.setEnabled(true);
            ((MyItemHolder) holder).kur500Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onPickButtonClick(v, position, AppConfig.KEY_KUR500);
                }
            });

            ((MyItemHolder) holder).picText.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur100Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur101Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur200Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur350Btn.setVisibility(View.GONE);

            ((MyItemHolder) holder).kur300Btn.setImageResource(R.drawable.status01_2_icon);
            ((MyItemHolder) holder).kur300Btn.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur300Btn.setEnabled(false);

            ((MyItemHolder) holder).kur310Btn.setImageResource(R.drawable.status03_2_icon);
            ((MyItemHolder) holder).kur310Btn.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur310Btn.setEnabled(false);

        }else if(packet.getStatus().equalsIgnoreCase(AppConfig.KEY_KUR400)){
            ((MyItemHolder) holder).kur350Btn.setImageResource(R.drawable.status06_1_icon);
            ((MyItemHolder) holder).kur350Btn.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur350Btn.setEnabled(true);
            ((MyItemHolder) holder).kur350Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onPickButtonClick(v, position, AppConfig.KEY_KUR350);
                }
            });

            ((MyItemHolder) holder).picText.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur100Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur101Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur200Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur400Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur500Btn.setVisibility(View.GONE);

            ((MyItemHolder) holder).kur300Btn.setImageResource(R.drawable.status01_2_icon);
            ((MyItemHolder) holder).kur300Btn.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur300Btn.setEnabled(false);

            ((MyItemHolder) holder).kur310Btn.setImageResource(R.drawable.status03_2_icon);
            ((MyItemHolder) holder).kur310Btn.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur310Btn.setEnabled(false);

        }else if(packet.getStatus().equalsIgnoreCase(AppConfig.KEY_KUR500)){
            ((MyItemHolder) holder).kur500Btn.setImageResource(R.drawable.status04_2_icon);
            ((MyItemHolder) holder).kur500Btn.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur500Btn.setEnabled(false);

            ((MyItemHolder) holder).picText.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur100Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur101Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur200Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur350Btn.setVisibility(View.GONE);
            ((MyItemHolder) holder).kur400Btn.setVisibility(View.GONE);

            ((MyItemHolder) holder).kur300Btn.setImageResource(R.drawable.status01_2_icon);
            ((MyItemHolder) holder).kur300Btn.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur300Btn.setEnabled(false);

            ((MyItemHolder) holder).kur310Btn.setImageResource(R.drawable.status03_2_icon);
            ((MyItemHolder) holder).kur310Btn.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur310Btn.setEnabled(false);

        }else{
            ((MyItemHolder) holder).picText.setVisibility(View.VISIBLE);
            ((MyItemHolder) holder).kur100Btn.setEnabled(false);
            ((MyItemHolder) holder).kur101Btn.setEnabled(false);
            ((MyItemHolder) holder).kur200Btn.setEnabled(false);
            ((MyItemHolder) holder).kur300Btn.setEnabled(false);
            ((MyItemHolder) holder).kur310Btn.setEnabled(false);
            ((MyItemHolder) holder).kur350Btn.setEnabled(false);
            ((MyItemHolder) holder).kur400Btn.setEnabled(false);
            ((MyItemHolder) holder).kur500Btn.setEnabled(false);
        }

        ((MyItemHolder) holder).updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onUpdateButtonClick(v, position);
            }
        });

        ((MyItemHolder) holder).waBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Packet p = data.get(position);
                itemClickListener.onWaButtonClick(v, position, p.getTeleponPengirim());
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class MyItemHolder extends RecyclerView.ViewHolder {
        protected TextView awbText;
        protected TextView namaPengirimText;
        protected TextView teleponPengirimText;
        protected TextView kotaAsalText;
        protected TextView kotaTujuanText;
        //protected TextView serviceCodeText;
        protected ImageView item_service;
        protected ImageView item_service_type;

        protected TextView picText;
        protected TextView statusText;
        protected TextView createdText;
        protected  ImageButton updateBtn;
        protected ImageButton kur101Btn;
        protected ImageButton kur100Btn;
        protected ImageButton kur200Btn;
        protected ImageButton kur300Btn;
        protected ImageButton kur310Btn;
        protected ImageButton kur350Btn;
        protected ImageButton kur400Btn;
        protected ImageButton kur500Btn;
        protected ImageButton kur900Btn;
        protected ImageButton kur910Btn;
        protected ImageButton kur999Btn;

        protected ImageButton waBtn;

        public MyItemHolder(View itemView) {
            super(itemView);
            this.awbText= (TextView) itemView.findViewById(R.id.tv_awb);
            this.namaPengirimText= (TextView) itemView.findViewById(R.id.tv_nama_pengirim);
            this.teleponPengirimText= (TextView) itemView.findViewById(R.id.tv_telepon_pengirim);
            this.kotaAsalText= (TextView) itemView.findViewById(R.id.tv_kota_asal);
            this.kotaTujuanText= (TextView) itemView.findViewById(R.id.tv_kota_tujuan);
            this.statusText= (TextView) itemView.findViewById(R.id.tv_status);
            this.createdText = (TextView) itemView.findViewById(R.id.tv_created);
            this.picText= (TextView) itemView.findViewById(R.id.tv_pic);
            //this.packetGrid = (GridLayout)itemView.findViewById(R.id.packet_grid);
            this.item_service = (ImageView) itemView.findViewById(R.id.item_service);
            this.item_service_type = (ImageView) itemView.findViewById(R.id.item_service_type);
            //this.serviceCodeText= (TextView) itemView.findViewById(R.id.packet_service);
            this.updateBtn= (ImageButton) itemView.findViewById(R.id.updateBtn);
            this.kur101Btn= (ImageButton) itemView.findViewById(R.id.kur101Btn);
            this.kur100Btn= (ImageButton) itemView.findViewById(R.id.kur100Btn);
            this.kur200Btn= (ImageButton) itemView.findViewById(R.id.kur200Btn);
            this.kur300Btn= (ImageButton) itemView.findViewById(R.id.kur300Btn);
            this.kur310Btn= (ImageButton) itemView.findViewById(R.id.kur310Btn);
            this.kur350Btn= (ImageButton) itemView.findViewById(R.id.kur350Btn);
            this.kur400Btn= (ImageButton) itemView.findViewById(R.id.kur400Btn);
            this.kur500Btn = (ImageButton) itemView.findViewById(R.id.kur500Btn);
            this.kur900Btn = (ImageButton) itemView.findViewById(R.id.kur900Btn);
            this.kur910Btn = (ImageButton) itemView.findViewById(R.id.kur910Btn);
            this.kur999Btn = (ImageButton) itemView.findViewById(R.id.kur999Btn);

            this.waBtn= (ImageButton) itemView.findViewById(R.id.waBtn);

        }
    }

    public interface OnItemClickListener {
        void onPickButtonClick(View view, int position, String status);
        void onUpdateButtonClick(View view, int position);
        void onWaButtonClick(View view, int position, String phoneNumber);
    }
}