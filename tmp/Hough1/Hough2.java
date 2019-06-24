import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

public class Hough extends JApplet{

   static final int XMAX=320;
   static final int YMAX=240;
   static final int RMAX=60;
   static final int THETA_MAX=1024;
   static final int RHO_MAX=400;
   static final float PIK=(float)Math.PI/THETA_MAX;

   //三角関数テーブル（サイン）
   float[] sn=new float[THETA_MAX];
   //三角関数テーブル（コサイン）
   float[] cs=new float[THETA_MAX];
   //半径計算用斜線長テーブル
   short[][] diagonal=new short[YMAX][XMAX];

   //二次元化した二値原画像データを格納
   byte[][] data=new byte[YMAX][XMAX];

   Image img_src;

   public void init(){

      //三角関数テーブルを作成
      for(int i=0;i<THETA_MAX;i++){
         sn[i]=(float)Math.sin(PIK*i);
         cs[i]=(float)Math.cos(PIK*i);
      }

      //斜線長テーブルを作成
      for(int y=0;y<YMAX;y++)
         for(int x=0;x<XMAX;x++)
            diagonal[y][x]=(short)(Math.sqrt(y*y+x*x)+0.5);

      //画像ファイルを読み込み、Image画像img_srcにする
      img_src=readImageFile("sample.gif");  

      //img_src画像を二次元配列data[y][x]に変換する
      changeTo2DDataArray(img_src,data);

   }

   //画像ファイルを読み込みImageクラスの画像にするメソッド
   public Image readImageFile(String filename){
        
      Image img=getImage(getDocumentBase(),filename);
      MediaTracker mtracker=new MediaTracker(this);
      mtracker.addImage(img,0);
      try{
         mtracker.waitForAll();
      }catch(Exception e){}
      return img;

   }

   //Image画像の黒色のみを二次元配列data[][]に変換するメソッド
   public void changeTo2DDataArray(Image img, byte[][] _data){

      int width=img.getWidth(this);
      int height=img.getHeight(this);
      int size=width*height;
      int[] rgb=new int[size];
      Color color;
      int r,g,b;

      //img画像の一次元RGB配列を得る
      PixelGrabber grabber=
         new PixelGrabber(img,0,0,width,height,rgb,0,width);
      try{
         grabber.grabPixels();
      }catch(InterruptedException e){}

      for(int i=0;i<size;i++){
         color=new Color(rgb[i]);
         r=color.getRed();
         g=color.getGreen();
         b=color.getBlue();
         if(r+g+b==0) _data[i/width][i % width]=1;     //黒
         else         _data[i/width][i % width]=0;     //白
      }

   }

    
   public void paint(Graphics g){

      //画像を描画する位置の設定
      int X0=10,Y0=10,X1=340,Y1=260;
      int x,y;

      //原画像img_srcを左に描画する
      g.drawImage(img_src,X0,Y0,this);

      // ---------------------- Hough変換 --------------------------

      //直線の場合 -------------------------------------------------
      int theta,rho;
      //直線検出用頻度カウンタ
      short[][] counter=new short[THETA_MAX][2*RHO_MAX];

      for(y=0;y<YMAX;y++)
         for(x=0;x<XMAX;x++)
            if(data[y][x]==1){
               for(theta=0;theta<THETA_MAX;theta++){
                  rho=(int)(x*cs[theta]+y*sn[theta]+0.5);
                  counter[theta][rho+RHO_MAX]++;
               }
            }

      //円の場合 ---------------------------------------------------
      int centerX,centerY,distX,distY,radius;
      //円検出用頻度カウンタ
      short[][][] counter1=new short[YMAX][XMAX][RMAX];

      for(y=0;y<YMAX;y++)
         for(x=0;x<XMAX;x++)
            if(data[y][x]==1){
               for(centerY=0;centerY<YMAX;centerY++){
                   distY=Math.abs(y-centerY);
                   if(distY>RMAX) continue;
                   for(centerX=0;centerX<XMAX;centerX++){
                     distX=Math.abs(x-centerX);
                     radius=diagonal[distY][distX];
                     if(radius>=RMAX) continue;
                     counter1[centerY][centerX][radius]++;
                  }
               }
            }

      // --------------------- Hough逆変換 -------------------------

      int end_flag;   //繰り返しを終了させるフラグ
      int count;      //検出された直線または円の個数カウンタ

      //画像データdata[y][x]を右に描画する
      for(y=0;y<YMAX;y++)
         for(x=0;x<XMAX;x++)
            if(data[y][x]==1)
               g.drawRect(X1+x,Y0+y,0,0);

      //直線の場合 -------------------------------------------------
      int counter_max;
      int theta_max=0;
      int rho_max=-RHO_MAX;

      g.setColor(Color.red);

      end_flag=0;
      count=0;

      do{
         count++;
         counter_max=0;         
         //counterが最大になるtheta_maxとrho_maxを求める
         for(theta=0;theta<THETA_MAX;theta++)
            for(rho=-RHO_MAX;rho<RHO_MAX;rho++)
               if(counter[theta][rho+RHO_MAX]>counter_max){
                  counter_max=counter[theta][rho+RHO_MAX];
                  //60ピクセル以下の直線になれば検出を終了
                  if(counter_max<=60) end_flag=1;
                  else                end_flag=0;
                  theta_max=theta;
                  rho_max=rho;
               }

         //検出した直線の描画
         //xを変化させてyを描く（垂直の線を除く）
         if(theta_max!=0){
            for(x=0;x<XMAX;x++){
               y=(int)((rho_max-x*cs[theta_max])/sn[theta_max]);
               if(y>=YMAX || y<0) continue;
               g.drawRect(X1+x,Y0+y,0,0);            
            }
         }

         //yを変化させてxを描く（水平の線を除く）
         if(theta_max!=THETA_MAX/2){
            for(y=0;y<YMAX;y++){
               x=(int)((rho_max-y*sn[theta_max])/cs[theta_max]);
               if(x>=XMAX || x<0) continue;
               g.drawRect(X1+x,Y0+y,0,0);
            }
         }

         //近傍の直線を消す
         for(int j=-10;j<=10;j++)
            for(int i=-30;i<=30;i++){
               if(theta_max+i<0){
                  theta_max+=THETA_MAX;
                  rho_max=-rho_max;
               }
               if(theta_max+i>=THETA_MAX){
                  theta_max-=THETA_MAX;
                  rho_max=-rho_max;
               }
               if(rho_max+j<-RHO_MAX || rho_max+j>=RHO_MAX)
                 continue;
               counter[theta_max+i][rho_max+RHO_MAX+j]=0;
            }
      //長さが60ピクセル以下か、直線が10本検出されたら終了
      }while(end_flag==0 && count<10);


      //円の場合 ---------------------------------------------------
      int counter1_max;

      int centerX_max=0;
      int centerY_max=0;
      int radius_max=0;

      g.setColor(Color.blue);

      end_flag=0;
      count=0;

      do{
         count++;
         counter1_max=0;         
         //counter1が最大になるcenterX_max、
         //centerY_maxとradius_maxを求める
         for(centerY=0;centerY<YMAX;centerY++)
            for(centerX=0;centerX<XMAX;centerX++)
               for(radius=0;radius<RMAX;radius++)
               if(counter1[centerY][centerX][radius]>counter1_max){
                  counter1_max=counter1[centerY][centerX][radius];
                  //100ピクセル以下の円になれば検出を終了
                  if(counter1_max<=100) end_flag=1; 
                  else                  end_flag=0;
                  centerY_max=centerY;
                  centerX_max=centerX;
                  radius_max=radius;

               }

         //近傍の円を消す
         for(int k=-5;k<=5;k++){
            if(centerY_max+k>=YMAX || centerY_max+k<0) continue;
            for(int j=-5;j<=5;j++){
               if(centerX_max+j>=XMAX || centerX_max+j<0) continue;
               for(int i=-5;i<=5;i++){
                  //if(radius_max+i>=RMAX || radius_max+i<0) continue;
                  if(radius_max+i<0) continue;
                  counter1[centerY_max+k]
                    [centerX_max+j][radius_max+i]=0;
               }
            }
         }          

         //検出した円の描画
         g.drawOval(X1+centerX_max-radius_max,
           Y0+centerY_max-radius_max,radius_max*2,radius_max*2);
      //大きさが100ピクセル以下か、円が5個検出されたら終了
      }while(end_flag==0 && count<5);

   }

}
