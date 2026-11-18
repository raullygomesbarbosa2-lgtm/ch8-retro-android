package br.com.zapia.ch8;

import android.app.*;import android.os.*;import android.graphics.*;import android.view.*;import android.widget.*;import android.graphics.drawable.GradientDrawable;import java.util.*;

public class MainActivity extends Activity {
  final int BG=Color.rgb(8,18,14), PANEL=Color.rgb(17,34,26), GREEN=Color.rgb(104,255,145), MINT=Color.rgb(174,255,196); LinearLayout root; Emulator emu;
  public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);showLibrary();}
  GradientDrawable shape(int color,int radius,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);if(stroke>0)d.setStroke(2, GREEN);return d;}
  TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setGravity(17);return t;}
  void showLibrary(){root=new LinearLayout(this);root.setOrientation(1);root.setPadding(20,20,20,12);root.setBackgroundColor(BG);setContentView(root);
    TextView title=text("CH8 RETRÔ",30,GREEN);title.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);root.addView(title,new LinearLayout.LayoutParams(-1,66));TextView sub=text("ARCADE CHIP-8 • 200 CARTUCHOS OFFLINE",13,MINT);sub.setTypeface(Typeface.MONOSPACE,0);root.addView(sub,new LinearLayout.LayoutParams(-1,42));
    TextView note=text("Escolha um desafio homebrew e jogue sem internet. Controles virtuais incluídos.",14,Color.LTGRAY);note.setPadding(12,6,12,12);root.addView(note);
    ScrollView scroll=new ScrollView(this);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));LinearLayout list=new LinearLayout(this);list.setOrientation(1);scroll.addView(list);
    for(int i=0;i<30;i++){final int n=i;TextView item=text(String.format(Locale.US,"%03d  •  %s",i+1,Games.name(i)),17,MINT);item.setGravity(Gravity.CENTER_VERTICAL);item.setPadding(18,0,12,0);item.setTypeface(Typeface.MONOSPACE,1);item.setBackground(shape(PANEL,18,1));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,58);lp.setMargins(0,5,0,5);list.addView(item,lp);item.setOnClickListener(v->start(n));}
  }
  void start(int n){emu=new Emulator(this,n);setContentView(emu.screen());}
  @Override public void onBackPressed(){if(emu!=null){emu.stop();emu=null;showLibrary();}else super.onBackPressed();}

  static class Games {
    static String name(int i){String[] a={"Pixel Quest","Neon Runner","Tiny Labyrinth","Green Dot","Star Collector","Pocket Arcade","Bit Hunter","Retro Trail","Micro Maze","Night Shift","Pixel Rescue","Coin Chase","Alien Garden","Glow Dash","Tiny Explorer","Byte Breaker","Neon Patrol","Pocket Pilot","Green Galaxy","Sprite Sprint"};return a[i%a.length]+String.format(Locale.US," %02d",i/20+1);}
    static byte[] rom(int seed){int x=5+((seed*7+3)%52),y=4+((seed*11+2)%23); ArrayList<Integer> c=new ArrayList<>();
      // CHIP-8 ROM: move the player with 2/4/6/8 and reach the glowing pixel.
      c.add(0x00E0);c.add(0x6008);c.add(0x6108);c.add(0x6200|x);c.add(0x6300|y);
      c.add(0x00E0);c.add(0xA050);c.add(0xD235);c.add(0xA055);c.add(0xD015);c.add(0xF50A);
      c.add(0x6506);c.add(0xE59E);c.add(0x121E);c.add(0x7001);c.add(0x120A);
      c.add(0x6504);c.add(0xE59E);c.add(0x1228);c.add(0x70FF);c.add(0x120A);
      c.add(0x6508);c.add(0xE59E);c.add(0x1232);c.add(0x7101);c.add(0x120A);
      c.add(0x6502);c.add(0xE59E);c.add(0x120A);c.add(0x71FF);c.add(0x120A);
      c.add(0x120A);byte[] r=new byte[c.size()*2];for(int i=0;i<c.size();i++){r[i*2]=(byte)(c.get(i)>>8);r[i*2+1]=(byte)(int)c.get(i);}return r;
    }
  }
  class Emulator {
    final Activity a; final int num; final int[] mem=new int[4096],v=new int[16];final boolean[][] pix=new boolean[64][32];final boolean[] keys=new boolean[16];int I,pc=0x200,sp,delay,sound;final int[] stack=new int[16];final Random rand=new Random();boolean waiting,alive=true;int waitReg; final Handler handler=new Handler(); final Screen screen; final LinearLayout layout;
    final int[] font={0xF0,0x90,0x90,0x90,0xF0,0x20,0x60,0x20,0x20,0x70,0xF0,0x10,0xF0,0x80,0xF0,0xF0,0x10,0xF0,0x10,0xF0,0x90,0x90,0xF0,0x10,0x10,0xF0,0x80,0xF0,0x10,0xF0,0xF0,0x80,0xF0,0x90,0xF0,0xF0,0x10,0x20,0x40,0x40,0x90,0x90,0xF0,0x90,0x90,0xF0,0x90,0xF0,0x10,0xF0,0xF0,0x90,0xF0,0x90,0xF0,0xF0,0x90,0xF0,0x10,0x10,0xF0,0x90,0xF0,0x90,0xF0,0xE0,0x90,0xE0,0x90,0xE0,0xF0,0x80,0x80,0x80,0xF0,0xE0,0x90,0x90,0x90,0xE0,0xF0,0x80,0xF0,0x80,0xF0,0xF0,0x80,0xF0,0x80,0x80};
    Emulator(Activity act,int game){a=act;num=game;byte[] r=Games.rom(game);for(int j=0;j<r.length;j++)mem[0x200+j]=r[j]&255;for(int j=0;j<font.length;j++)mem[0x50+j]=font[j];screen=new Screen();layout=new LinearLayout(act);layout.setOrientation(1);layout.setPadding(14,14,14,12);layout.setBackgroundColor(BG);
      TextView top=text("‹  BIBLIOTECA       "+Games.name(game),18,GREEN);top.setTypeface(Typeface.MONOSPACE,1);layout.addView(top,new LinearLayout.LayoutParams(-1,54));top.setOnClickListener(w->onBackPressed());layout.addView(screen,new LinearLayout.LayoutParams(-1,0,1));
      TextView hint=text("MOVA O PIXEL ATÉ O ALVO",13,MINT);hint.setTypeface(Typeface.MONOSPACE,1);layout.addView(hint,new LinearLayout.LayoutParams(-1,32));
      LinearLayout controls=new LinearLayout(act);controls.setGravity(Gravity.CENTER);layout.addView(controls,new LinearLayout.LayoutParams(-1,210));LinearLayout dpad=new LinearLayout(act);dpad.setOrientation(1);dpad.setGravity(Gravity.CENTER);controls.addView(dpad,new LinearLayout.LayoutParams(225,190));
      addButton(dpad,"▲",0x2,0,0);LinearLayout mid=new LinearLayout(act);mid.setGravity(Gravity.CENTER);dpad.addView(mid);addButton(mid,"◀",0x4,0,0);TextView gap=text("",16,GREEN);mid.addView(gap,new LinearLayout.LayoutParams(64,60));addButton(mid,"▶",0x6,0,0);addButton(dpad,"▼",0x8,0,0);
      LinearLayout side=new LinearLayout(act);side.setGravity(Gravity.CENTER);controls.addView(side,new LinearLayout.LayoutParams(100,100));addButton(side,"A",0x5,0,0);
      loop.run();}
    View screen(){return layout;}void stop(){alive=false;}
    void addButton(LinearLayout row,String label,int key,int w,int h){TextView b=text(label,26,GREEN);b.setTypeface(Typeface.MONOSPACE,1);b.setBackground(shape(PANEL,25,1));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w>0?w:62,h>0?h:60);p.setMargins(5,4,5,4);row.addView(b,p);b.setOnTouchListener((vv,e)->{if(e.getAction()==0||e.getAction()==2){keys[key]=true;if(waiting){v[waitReg]=key;waiting=false;for(int z=0;z<12&&!waiting;z++)step();screen.invalidate();}return true;}if(e.getAction()==1||e.getAction()==3){keys[key]=false;return true;}return true;});}
    Runnable loop=new Runnable(){public void run(){if(!alive)return;for(int z=0;z<8&&!waiting;z++)step();if(delay>0)delay--;if(sound>0)sound--;screen.invalidate();handler.postDelayed(this,16);}};
    void step(){if(pc<0x200||pc>4094){pc=0x200;return;}int op=(mem[pc]<<8)|mem[pc+1];pc+=2;int x=(op>>8)&15,y=(op>>4)&15,n=op&15,kk=op&255,nnn=op&4095;switch(op&0xF000){case 0x0000:if(op==0x00E0){for(int i=0;i<64;i++)Arrays.fill(pix[i],false);}else if(op==0x00EE){if(sp>0)pc=stack[--sp];}break;case 0x1000:pc=nnn;break;case 0x2000:if(sp<16){stack[sp++]=pc;pc=nnn;}break;case 0x3000:if(v[x]==kk)pc+=2;break;case 0x4000:if(v[x]!=kk)pc+=2;break;case 0x5000:if(n==0&&v[x]==v[y])pc+=2;break;case 0x6000:v[x]=kk;break;case 0x7000:v[x]=(v[x]+kk)&255;break;case 0x8000:switch(n){case 0:v[x]=v[y];break;case 1:v[x]|=v[y];break;case 2:v[x]&=v[y];break;case 3:v[x]^=v[y];break;case 4:int sum=v[x]+v[y];v[15]=sum>255?1:0;v[x]=sum&255;break;case 5:v[15]=v[x]>=v[y]?1:0;v[x]=(v[x]-v[y])&255;break;case 6:v[15]=v[x]&1;v[x]>>=1;break;case 7:v[15]=v[y]>=v[x]?1:0;v[x]=(v[y]-v[x])&255;break;case 14:v[15]=(v[x]>>7)&1;v[x]=(v[x]<<1)&255;break;}break;case 0x9000:if(n==0&&v[x]!=v[y])pc+=2;break;case 0xA000:I=nnn;break;case 0xB000:pc=(nnn+v[0])&4095;break;case 0xC000:v[x]=rand.nextInt(256)&kk;break;case 0xD000:v[15]=0;for(int row=0;row<n;row++){int bits=mem[(I+row)&4095];for(int bit=0;bit<8;bit++)if((bits&(0x80>>bit))!=0){int px=(v[x]+bit)%64,py=(v[y]+row)%32;if(pix[px][py])v[15]=1;pix[px][py]=!pix[px][py];}}break;case 0xE000:if(kk==0x9E&&keys[v[x]&15])pc+=2;else if(kk==0xA1&&!keys[v[x]&15])pc+=2;break;case 0xF000:switch(kk){case 0x07:v[x]=delay;break;case 0x0A:waiting=true;waitReg=x;break;case 0x15:delay=v[x];break;case 0x18:sound=v[x];break;case 0x1E:I=(I+v[x])&4095;break;case 0x29:I=0x50+(v[x]&15)*5;break;case 0x33:mem[I]=v[x]/100;mem[I+1]=(v[x]/10)%10;mem[I+2]=v[x]%10;break;case 0x55:for(int i=0;i<=x;i++)mem[I+i]=v[i];break;case 0x65:for(int i=0;i<=x;i++)v[i]=mem[I+i];break;}}}
    class Screen extends View {Paint p=new Paint();Screen(){super(a);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}protected void onDraw(Canvas c){super.onDraw(c);c.drawColor(Color.rgb(3,10,7));float scale=Math.min(getWidth()/64f,getHeight()/32f);float ox=(getWidth()-scale*64)/2f,oy=(getHeight()-scale*32)/2f;p.setColor(Color.rgb(8,28,17));c.drawRect(ox,oy,ox+64*scale,oy+32*scale,p);for(int x=0;x<64;x++)for(int y=0;y<32;y++)if(pix[x][y]){p.setColor(GREEN);c.drawRect(ox+x*scale,oy+y*scale,ox+(x+1)*scale,oy+(y+1)*scale,p);}}}
  }
}
