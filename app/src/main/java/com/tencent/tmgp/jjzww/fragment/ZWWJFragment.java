package com.tencent.tmgp.jjzww.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gatz.netty.utils.NettyUtils;
import com.tencent.tmgp.jjzww.R;
import com.tencent.tmgp.jjzww.activity.ctrl.view.CtrlActivity;
import com.tencent.tmgp.jjzww.adapter.ZWWAdapter;
import com.tencent.tmgp.jjzww.base.BaseFragment;
import com.tencent.tmgp.jjzww.bean.LoginInfo;
import com.tencent.tmgp.jjzww.bean.Marquee;
import com.tencent.tmgp.jjzww.bean.Result;
import com.tencent.tmgp.jjzww.bean.VideoBackBean;
import com.tencent.tmgp.jjzww.bean.ZwwRoomBean;
import com.tencent.tmgp.jjzww.model.http.HttpManager;
import com.tencent.tmgp.jjzww.model.http.RequestSubscriber;
import com.tencent.tmgp.jjzww.utils.UrlUtils;
import com.tencent.tmgp.jjzww.utils.UserUtils;
import com.tencent.tmgp.jjzww.utils.Utils;
import com.tencent.tmgp.jjzww.view.EmptyLayout;
import com.tencent.tmgp.jjzww.view.GlideImageLoader;
import com.tencent.tmgp.jjzww.view.MarqueeView;
import com.tencent.tmgp.jjzww.view.MyToast;
import com.tencent.tmgp.jjzww.view.SpaceItemDecoration;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * Created by hongxiu on 2017/9/25.
 */
public class ZWWJFragment extends BaseFragment {
    private static final String TAG = "ZWWJFragment";
    @BindView(R.id.zww_recyclerview)
    RecyclerView zwwRecyclerview;
    @BindView(R.id.zww_emptylayout)
    EmptyLayout zwwEmptylayout;
    Unbinder unbinder;
    //    @BindView(R.id.rolling_tv)
//    MyTextSwitcher rollingTv;
    @BindView(R.id.marqueeview)
    MarqueeView marqueeview;
    @BindView(R.id.zww_banner)
    Banner zwwBanner;
   // Unbinder unbinder1;

    private List<ZwwRoomBean> roomBeens = new ArrayList<>();
    private ZWWAdapter zwwAdapter;
    private String sessionId;
    private EmptyLayout.OnClickReTryListener onClickReTryListener;
    private List<VideoBackBean> playBackBeanList = new ArrayList<>();
    private List<Marquee> marquees = new ArrayList<>();
    private List<String> mListImage;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_zww;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        initData();
        onClick();
        getUserList();
        initBanner();
    }

    private void getUserList() {
        HttpManager.getInstance().getUserList(new RequestSubscriber<Result<LoginInfo>>() {
            @Override
            public void _onSuccess(Result<LoginInfo> listRankBeanResult) {
                playBackBeanList = listRankBeanResult.getData().getPlayback();
                for (int i = 0; i < playBackBeanList.size(); i++) {
                    Marquee marquee = new Marquee();
                    String s = "恭喜" + "<font color='#FF0000'>" + playBackBeanList.get(i).getUSERNAME() + "</font>"
                            + "用户抓中一个" + playBackBeanList.get(i).getDOLLNAME();
                    marquee.setTitle(s);
                    marquee.setImgUrl(UrlUtils.USERFACEIMAGEURL + playBackBeanList.get(i).getIMAGE_URL());
                    marquees.add(marquee);
                }
                marqueeview.setImage(true);
                marqueeview.startWithList(marquees);
            }

            @Override
            public void _onError(Throwable e) {

            }
        });
    }




    private void initData() {
        Utils.showLogE(TAG, "afterCreate:::::>>>>" + roomBeens.size());
        dismissEmptyLayout();
        zwwAdapter = new ZWWAdapter(getActivity(), roomBeens);
        zwwRecyclerview.setLayoutManager(new GridLayoutManager(getContext(), 2));
        zwwRecyclerview.addItemDecoration(new SpaceItemDecoration(15));
        zwwRecyclerview.setAdapter(zwwAdapter);
        if (onClickReTryListener != null) {
            zwwEmptylayout.setOnClickReTryListener(onClickReTryListener);
        }
    }

    private void onClick() {
        zwwAdapter.setmOnItemClickListener(onItemClickListener);
    }

    public void notifyAdapter(List<ZwwRoomBean> rooms) {
        roomBeens = rooms;
        zwwAdapter.notify(roomBeens);
    }

    public void setOnClickEmptyListener(EmptyLayout.OnClickReTryListener o) {
        this.onClickReTryListener = o;
    }

    public void showError() {
        zwwEmptylayout.showEmpty();
    }

    public void showLoading() {
        zwwEmptylayout.showLoading();
    }

    public void dismissEmptyLayout() {
        zwwEmptylayout.dismiss();
    }

    public void setSessionId(String id, boolean isReconnect) {
        this.sessionId = id;
        UserUtils.setNettyInfo(sessionId, UserUtils.USER_ID, "", isReconnect);
        UserUtils.doNettyConnect(NettyUtils.LOGIN_TYPE_TENCENT);
    }

    public ZWWAdapter.OnItemClickListener onItemClickListener =
            new ZWWAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    if ((roomBeens.size() > 0) && (!Utils.isEmpty(sessionId))) {
                        String room_id = roomBeens.get(position).getDOLL_ID();
                        boolean room_status = false;
                        UserUtils.setNettyInfo(sessionId, UserUtils.USER_ID, room_id, false);
                        if (roomBeens.get(position).getDOLL_STATE().equals("0")) {
                            room_status = true;
                        } else if (roomBeens.get(position).getDOLL_STATE().equals("1")) {
                            room_status = false;
                        }
                        String url1 = roomBeens.get(position).getCAMERA_NAME_01();
                        String url2 = roomBeens.get(position).getCAMERA_NAME_02();
                        if (!TextUtils.isEmpty(url2)) {
                            enterNext(roomBeens.get(position).getDOLL_NAME(),
                                    url1, url2,
                                    room_status,
                                    String.valueOf(roomBeens.get(position).getDOLL_GOLD()),
                                    roomBeens.get(position).getDOLL_ID());
                        } else {
                            Utils.showLogE(TAG, "当前设备没有配置摄像头!");
                        }
                    }
                }
            };

    private void enterNext(String name, String camera1, String camera2, boolean status, String gold, String id) {
        Intent intent = new Intent(getActivity(), CtrlActivity.class);
        intent.putExtra(Utils.TAG_ROOM_NAME, name);
        intent.putExtra(Utils.TAG_URL_MASTER, camera1);
        intent.putExtra(Utils.TAG_URL_SECOND, camera2);
        intent.putExtra(Utils.TAG_ROOM_STATUS, status);
        intent.putExtra(Utils.TAG_DOLL_GOLD, gold);
        intent.putExtra(Utils.TAG_DOLL_Id, id);
        startActivity(intent);
    }


    @Override
    public void onResume() {
        super.onResume();
        //getUserList();
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        // TODO: inflate a fragment view
//        View rootView = super.onCreateView(inflater, container, savedInstanceState);
//        unbinder1 = ButterKnife.bind(this, rootView);
//        return rootView;
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        unbinder1.unbind();
//    }

    //banner轮播
    private void initBanner(){
        //设置Banner样式
        zwwBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
        //设置图片加载器
        zwwBanner.setImageLoader(new GlideImageLoader());
        //实例化图片集合
        mListImage = new ArrayList<>();
        //将图片放入集合中
        mListImage.add("http://img.hc360.com/auto-a/info/images/200803/8998419-10.jpg");
        mListImage.add("http://img.ichemo.cn/model/5188ebc96c74dd.jpg");
        mListImage.add("http://i.ebayimg.com/00/s/MzU1WDcwOQ==/%24%28KGrHqR,%21hYFDuguSMpNBQ8e+seDCQ~~60_1.JPG?set_id=880000500F");
        mListImage.add("http://a4.qpic.cn/psb?/V129YDsp1WxQ4t/65kWq9ygL.Sw4G9*hj30d0BXvNLS9RworM3lIuw7lfc!/c/dPMAAAAAAAAA&ek=1&kp=1&pt=0&bo=9AH6AAAAAAARFy8!&t=5&vuin=2422172415&tm=1513652400&sce=60-2-2&rf=newphoto&t=5");
        mListImage.add("http://a4.qpic.cn/psb?/V129YDsp1WxQ4t/IkSbTgiJL9I7Ec7FQwySpUwLkkXznq2td3ZzvKH0wKA!/c/dPMAAAAAAAAA&ek=1&kp=1&pt=0&bo=9AH6AAAAAAARFy8!&t=5&vuin=2422172415&tm=1513652400&sce=60-2-2&rf=newphoto&t=5");
        mListImage.add("http://a4.qpic.cn/psb?/V129YDsp1WxQ4t/22f4y.iBc4SJJqW5TbKIffpgD8.2lkp2cACnn*hM1j8!/b/dPMAAAAAAAAA&ek=1&kp=1&pt=0&bo=9AH6AAAAAAARFy8!&vuin=2422172415&tm=1513656000&sce=60-2-2&rf=viewer_4");
        mListImage.add("http://a4.qpic.cn/psb?/V129YDsp1WxQ4t/*OrL7JyltR.4ruHWBAdgs5zPVEMF83H*7e3mxfb5dUw!/c/dD8BAAAAAAAA&ek=1&kp=1&pt=0&bo=9AH6AAAAAAARFy8!&t=5&vuin=2422172415&tm=1513652400&sce=60-2-2&rf=newphoto&t=5");
        mListImage.add("http://m.qpic.cn/psb?/V129YDsp1WxQ4t/ZnDBmT03YYQT61mv20vHsah5eHPXlSDmRruTCStBK.E!/b/dPIAAAAAAAAA&bo=wgHSAAAAAAARByE!&rf=viewer_4");
        mListImage.add("http://m.qpic.cn/psb?/V129YDsp1WxQ4t/3KAvFL2XxdKQYeCqqEhtJijB.J5gT6btdSVsFB91GUU!/b/dPIAAAAAAAAA&bo=wgHSAAAAAAARFzE!&rf=viewer_4");
        //设置Banner图片集合
        zwwBanner.setImages(mListImage);
        //设置Banner动画效果
        zwwBanner.setBannerAnimation(Transformer.DepthPage);
        //设置轮播时间
        zwwBanner.setDelayTime(2000);
        //设置指示器位置（当banner模式中有指示器时）
        zwwBanner.setIndicatorGravity(BannerConfig.CENTER);
        //Banner设置方法全部调用完毕时最后调用
        zwwBanner.start();
        zwwBanner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                MyToast.getToast(getContext(),"您点击了第"+(position+1)+"张图片").show();
            }
        });
    }

    //如果你需要考虑更好的体验，可以这么操作
    @Override
    public void onStart() {
        super.onStart();
        //开始轮播
       zwwBanner.startAutoPlay();
    }

    @Override
    public void onStop() {
        super.onStop();
        //结束轮播
        zwwBanner.stopAutoPlay();
    }

}
