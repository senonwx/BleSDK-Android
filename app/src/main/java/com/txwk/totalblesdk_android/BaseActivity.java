package com.txwk.totalblesdk_android;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.zhouwei.mzbanner.MZBannerView;
import com.zhouwei.mzbanner.holder.MZHolderCreator;
import com.zhouwei.mzbanner.holder.MZViewHolder;
import java.util.List;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * BaseActivity
 */
public abstract class BaseActivity extends AppCompatActivity {

    private SweetAlertDialog sweetAlertDialog;
    private MZBannerView banner;
    private List<Integer> imgList;
    private List<String> tipList;
    public boolean isDestory = false;//是否已经销毁

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutId());
        init();

    }

    /**
     * 子类初始化布局
     * @return layout id
     */
    public abstract int getLayoutId();

    /**
     * 子类初始化其他
     */
    public abstract void init();

    /**
     * 是否出现banner
     * @param banner  banner
     * @param imgList banner图片资源
     * @param tipList banner提示语
     */
    public void initBanner(MZBannerView banner, List<Integer> imgList,List<String> tipList){
        this.banner = banner;
        this.imgList = imgList;
        this.tipList = tipList;
        // 设置数据
        banner.setPages(imgList, new MZHolderCreator<BannerViewHolder>() {
            @Override
            public BannerViewHolder createViewHolder() {
                return new BannerViewHolder();
            }
        });
        banner.setDuration(3);
    }

    public class BannerViewHolder implements MZViewHolder<Integer> {
        private ImageView mImageView;
        private TextView banner_tv;
        @Override
        public View createView(Context context) {
            // 返回页面布局
            View view = LayoutInflater.from(context).inflate(R.layout.banner_bluetooth_item,null);
            mImageView = (ImageView) view.findViewById(R.id.banner_image);
            banner_tv = (TextView) view.findViewById(R.id.banner_tv);
            return view;
        }
        @Override
        public void onBind(Context context, int position, Integer data) {
            // 数据绑定
            mImageView.setImageResource(imgList.get(position));
            banner_tv.setText(tipList.get(position));
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if(banner != null){
            banner.pause();//暂停轮播
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(banner != null){
            banner.start();//开始轮播
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestory = true;
    }

    //显示dialog
    public void addSweetDialog(String content) {
        if(sweetAlertDialog != null && sweetAlertDialog.isShowing()){
            sweetAlertDialog.dismiss();
        }
        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setTitleText(content);
        sweetAlertDialog.setCanceledOnTouchOutside(false);
        sweetAlertDialog.show();

    }

    //取消dialog
    public void dissmissDialog() {
        if (sweetAlertDialog != null) {
            sweetAlertDialog.dismiss();
        }
    }
}
