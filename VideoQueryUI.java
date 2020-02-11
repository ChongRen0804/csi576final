import javax.swing.*;
import javax.sound.sampled.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;


public class VideoQueryUI extends JFrame implements ActionListener {
    private ArrayList<BufferedImage> images; 
    private ArrayList<BufferedImage> dbImages;
    private PlaySound playSound;
    private PlaySound playDBSound;
    static final int frameRate = 30;
    private JLabel imageLabel;
    private JLabel resultImageLabel;
    
    private JLabel errorLabel;
    private String errorsg;
    private TextField queryField;
    private Button playButton;
    private Button pauseButton;
    private Button stopButton;
    private Button resultPlayButton;
    private Button resultPauseButton;
    private Button resultStopButton;
    private Button loadQueryButton;
    private Button loadResultButton;
    private Button searchButton;
    private List resultListDisplay;
    private Map<String, Double> resultMap;
    private Map<String, Double> sortedResultMap;
    private ArrayList<Double> resultList;
    private ArrayList<String> resultListRankedNames;
    private String fileName;
    private JSlider slider;
    private int playStatus = 3;//1 for play, 2 for pause, 3 for stop
    private int resultPlayStatus = 3;
    private Thread playingThread;
    private Thread playingDBThread;
    private Thread audioThread;
    private Thread audioDBThread;
    private JPanel swingImagePanel;
    private int currentFrameNum = 0;
    private int currentDBFrameNum = 0;
    private int totalFrameNum = 150;
    private int totalDBFrameNum = 600;
    private String fileFolder = "/Users/rc/Desktop/576final/query";
    private String dbFileFolder = "/Users/rc/Desktop/576final/db";
    static final int WIDTH = 352;
    static final int HEIGHT = 288;
    private compareAndsearch searchClass;
    private Map<String, Integer> similarFrameMap;

    
	public VideoQueryUI(ArrayList<BufferedImage> imgs) {
		
		this.images = imgs;

		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLayout(null);
		this.setMinimumSize(new Dimension(800,600));
		//Query Panel
		JPanel queryPanel = new JPanel();
		queryPanel.setLayout(null);
		queryPanel.setBounds(10, 20, 300,200);
		queryField = new TextField(13);
		queryField.setBounds(70,20,200,20);
		JLabel queryLabel = new JLabel("Query: ");
		queryLabel.setBounds(20,20,50,20);
		queryPanel.add(queryLabel);
		queryPanel.add(queryField);

		loadQueryButton = new Button("Load Query Video");
		loadQueryButton.setBounds(70,50,200,20);

		errorLabel = new JLabel("");
		errorLabel.setForeground(Color.RED);

		searchButton = new Button("Search");
		searchButton.setFont(new Font("monspaced", Font.BOLD, 10));
		searchButton.setBounds(70,80,200,20);
		queryPanel.add(loadQueryButton);
//	    queryPanel.add(errorLabel);
		queryPanel.add(searchButton);

		this.add(queryPanel);
	    loadQueryButton.addActionListener(this);


	    searchButton.addActionListener(this);

	    
	    //Result Panel

		Panel resultPanel = new Panel();
		resultPanel.setLayout(null);
		resultPanel.setBounds(400, 0, 400,250);
		resultListDisplay = new List(3);
		resultListDisplay.setBounds(30,40,200,100);

		JLabel matchedVideosLabel = new JLabel("Matched Videos:");
		matchedVideosLabel.setBounds(30,20, 200,20);
		resultList = new ArrayList<Double>(7);
		resultListRankedNames = new ArrayList<String>(7);

		resultPanel.add(matchedVideosLabel);
		resultPanel.add(resultListDisplay, BorderLayout.SOUTH);
		loadResultButton = new Button("Load Selected Video");
		loadResultButton.setBounds(30,150, 200,20);
		loadResultButton.addActionListener(this);
		resultPanel.add(loadResultButton);

		swingImagePanel = new JPanel() {
			@Override
			public void paint(Graphics g) {
				int userSelect = resultListDisplay.getSelectedIndex() - 1;
				super.paint(g);
				if(userSelect!=-2){
					g.setColor(Color.RED);
					// 判断如果 data buffer 为空，do nothing, 即空白的 swingImagePanel
					// else : 执行如下
					// Paint your curve here, this is a demo
					for(int a=0;a<searchClass.curveValue[userSelect].length;a++){
						//System.out.print("!!!="+searchClass.curveValue[userSelect][a]);
						int y=100-(int)(searchClass.curveValue[userSelect][a]*100);
						y=(int)y*30/100;
						int x=(int)a*350/600;
						g.drawLine(x,y,x,y);
						//System.out.println("x="+x+",y="+y);
					}

					for (int i = 0; i < 100; i++) {
//					int x = i * 2;
//					int y = (int)((i+0.0)/100 * 30);
//					g.drawLine(x,y,x,200);
//					g.drawLine(x+1,y,x+1,200);
					//g.drawLine(i,20,i,20);//画一条线段


					}
				}
				//System.out.print("userSelect!!!="+userSelect);

			}
		};
		swingImagePanel.setBounds(30, 180, 350,30);
		swingImagePanel.setBackground(Color.WHITE);
		resultPanel.add(swingImagePanel);

		slider = new JSlider(0, 100, 0);  // min, max, current
		slider.setBounds(15, 220, 370,20);
		resultPanel.add(slider);
		/*slider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				currentDBFrameNum = slider.getValue();
				resultImageLabel.setIcon(new ImageIcon(dbImages.get(currentDBFrameNum)));
				System.out.println(currentDBFrameNum);

				stopDBVideo();
				resultPlayStatus = 2;
				try{
					playDBSound.jump(20 * 1000000 * (currentDBFrameNum / images.size()));
					System.out.println(20 * 1000000 * (currentDBFrameNum / images.size()));
				}catch (PlayWaveException ee) {
					ee.printStackTrace();
					return;
				}catch (UnsupportedAudioFileException u){

				}catch (IOException i){

				}catch (LineUnavailableException l){

				}


				playDBVideo();
			}
		});*/
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				resultImageLabel.setIcon(new ImageIcon(dbImages.get(slider.getValue())));
			}
		});
		//slider.setStringPainted(true);
		add(resultPanel);


	    
	    //Video List Panel
		Panel listPanel = new Panel();
		listPanel.setBounds(5,250,800, 400);
		listPanel.setLayout(new GridLayout(2, 2));
		this.imageLabel = new JLabel(new ImageIcon(images.get(currentFrameNum)));
		this.resultImageLabel = new JLabel(new ImageIcon(images.get(currentFrameNum)));
		Panel imagePanel = new Panel();
		imagePanel.add(this.imageLabel);
		Panel resultImagePanel = new Panel();
		resultImagePanel.add(this.resultImageLabel);
		listPanel.add(imagePanel);
		listPanel.add(resultImagePanel);
	    
	    //Control Panel
	    Panel controlPanel = new Panel();
	    Panel resultControlPanel = new Panel();
	    
	    playButton = new Button("PLAY");
	    playButton.addActionListener(this);
	    resultPlayButton = new Button("PLAY");
	    resultPlayButton.addActionListener(this);
	    controlPanel.add(playButton);
	    resultControlPanel.add(resultPlayButton);
	    
	    pauseButton = new Button("PAUSE");
	    pauseButton.addActionListener(this);
	    resultPauseButton = new Button("PAUSE");
	    resultPauseButton.addActionListener(this);
	    controlPanel.add(pauseButton);
	    resultControlPanel.add(resultPauseButton);
	    
	    stopButton = new Button("STOP");
	    stopButton.addActionListener(this);
	    resultStopButton = new Button("STOP");
	    resultStopButton.addActionListener(this);
	    controlPanel.add(stopButton);
	    resultControlPanel.add(resultStopButton);
	    resultControlPanel.add(errorLabel);
	    
	    listPanel.add(controlPanel);
	    listPanel.add(resultControlPanel);
	    add(listPanel);
	    
	    searchClass = new compareAndsearch();
	    try {
			searchClass.init();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setImages(ArrayList<BufferedImage> images){
		this.images = images;
	}
	
	public void showUI() {
	    pack();
	    setVisible(true);
	}


	private void playVideo() {
		playingThread = new Thread() {
            public void run() {
	            System.out.println("Start playing video: " + fileName);
	          	for (int i = currentFrameNum; i < totalFrameNum; i++) {
	          	  	imageLabel.setIcon(new ImageIcon(images.get(i)));
	          	    try {
	                  	sleep(1000/frameRate);
	          	    } catch (InterruptedException e) {
	          	    	if(playStatus == 3) {
	          	    		currentFrameNum = 0;
	          	    	} else {
	          	    		currentFrameNum = i;
	          	    	}
	          	    	imageLabel.setIcon(new ImageIcon(images.get(currentFrameNum)));
	                  	currentThread().interrupt();
	                  	break;
	                }
	          	}
	          	if(playStatus < 2) {
	          		playStatus = 3;
		            currentFrameNum = 0;
	          	}
	            System.out.println("End playing video: " + fileName);
	        }
	    };
	    audioThread = new Thread() {
            public void run() {
                try {
        	        playSound.play();
        	    } catch (PlayWaveException e) {
        	        e.printStackTrace();
        	        errorLabel.setText(e.getMessage());
        	        return;
        	    }
	        }
	    };
	    audioThread.start();
	    playingThread.start();
	}
	
	private void playDBVideo() {
		playingDBThread = new Thread() {
            public void run() {
	            System.out.println("Start playing result video: " + fileName);
	          	for (int i = currentDBFrameNum; i < totalDBFrameNum; i++) {
	          	  	resultImageLabel.setIcon(new ImageIcon(dbImages.get(i)));
	          	    try {
	                  	sleep(1000/frameRate);
	          	    } catch (InterruptedException e) {
	          	    	if(resultPlayStatus == 3) {
	          	    		currentDBFrameNum = 0;
	          	    	} else {
	          	    		currentDBFrameNum = i;
	          	    	}
	          	    	resultImageLabel.setIcon(new ImageIcon(dbImages.get(currentDBFrameNum)));
	                  	currentThread().interrupt();
	                  	break;
	                }
	          	}
	          	if(resultPlayStatus < 2) {
	          		resultPlayStatus = 3;
			        currentDBFrameNum = 0;
	          	}
	          	System.out.println("End playing result video: " + fileName);
	        }
	    };
	    audioDBThread = new Thread() {
            public void run() {
                try {
        	        playDBSound.play();
        	    } catch (PlayWaveException e) {
        	        e.printStackTrace();
        	        errorLabel.setText(e.getMessage());
        	        return;
        	    }
	        }
	    };
	    audioDBThread.start();
	    playingDBThread.start();
	}
	
	private void pauseVideo() throws InterruptedException {
		if(playingThread != null) {
			playingThread.interrupt();
			audioThread.interrupt();
			playSound.pause();
			playingThread = null;
			audioThread = null;
		}
	}
	
	private void pauseDBVideo() throws InterruptedException {
		if(playingDBThread != null){
			playingDBThread.interrupt();
			audioDBThread.interrupt();
			playDBSound.pause();
			playingDBThread = null;
			audioDBThread = null;
		}
	}
	
	private void stopVideo() {
		if(playingThread != null) {
			playingThread.interrupt();
			audioThread.interrupt();
			playSound.stop();
			playingThread = null;
			audioThread = null;
		} else {
			currentFrameNum = 0;
			displayScreenShot();
		}
	}
	
	private void stopDBVideo() {
		if(playingDBThread != null) {
			playingDBThread.interrupt();
			audioDBThread.interrupt();
			playDBSound.stop();
			playingDBThread = null;
			audioDBThread = null;
		} else {
			currentDBFrameNum = 0;
			displayDBScreenShot();
		}
	}
	
	private void displayScreenShot() {
		Thread initThread = new Thread() {
            public void run() {
	          	imageLabel.setIcon(new ImageIcon(images.get(currentFrameNum)));  	   
	        }
	    };
	    initThread.start();
	}
	
	private void displayDBScreenShot() {
		Thread initThread = new Thread() {
            public void run() {
            	resultImageLabel.setIcon(new ImageIcon(dbImages.get(currentDBFrameNum)));  	   
	        }
	    };
	    initThread.start();
	}
	
	private void updateSimilarFrame() {
		int userSelect = resultListDisplay.getSelectedIndex() - 1;
		String userSelectStr = resultListRankedNames.get(userSelect);
		Integer frm = similarFrameMap.get(userSelectStr);
		errorsg = "The most similar clip is from frame " + (frm+1) + " to frame " + (frm+151) + ".";
	    Thread initThread = new Thread() {
            public void run() {
            	errorLabel.setText(errorsg);  	   
	        }
	    };
	    initThread.start();
	}
	
	private void loadVideo(String userInput) {
		System.out.println("Start loading query video contents.");
	    try {
	      if(userInput == null || userInput.isEmpty()){
	    	  return;
	      }
	      //every query video in has 150 frames
	      images = new ArrayList<BufferedImage>();
	      for(int i=1; i<=150; i++) {
	    	  String fileNum = "00";
	    	  if(i < 100 && i > 9) {
	    		  fileNum = "0";
	    	  } else if(i > 99) {
	    		  fileNum = "";
	    	  }
	    	  String fullName = fileFolder + "/" + userInput + "/" + userInput + "_" +fileNum + new Integer(i).toString() + ".rgb";
			  //String fullName = fileFolder + "/" + userInput + "/" + userInput +fileNum + new Integer(i).toString() + ".rgb";
	    	  String audioFilename = fileFolder + "/" + userInput + "/" + userInput + ".wav";
	    	  
	    	  File file = new File(fullName);
	    	  InputStream is = new FileInputStream(file);

	   	      long len = file.length();
		      byte[] bytes = new byte[(int)len];
		      int offset = 0;
	          int numRead = 0;
	          while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	              offset += numRead;
	          }
	          System.out.println("Start loading frame: " + fullName);
	    	  int index = 0;
	          BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	          for (int y = 0; y < HEIGHT; y++) {
	            for (int x = 0; x < WIDTH; x++) {
	   				byte r = bytes[index];
	   				byte g = bytes[index+HEIGHT*WIDTH];
	   				byte b = bytes[index+HEIGHT*WIDTH*2]; 
	   				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
	    			image.setRGB(x,y,pix);
	    			index++;
	    		}
	    	  }
	          images.add(image);
	          is.close();

	          playSound = new PlaySound(audioFilename);
	          System.out.println("End loading query frame: " + fullName);
	      }//end for
	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	      errorLabel.setText(e.getMessage());
	    } catch (IOException e) {
	      e.printStackTrace();
	      errorLabel.setText(e.getMessage());
	    }
	    this.playStatus = 3;
	    currentFrameNum = 0;
	    totalFrameNum = images.size();
	    displayScreenShot();
	    System.out.println("End loading query video contents.");
	}
	
	
	private void loadDBVideo(String dbVideoName) {
		System.out.println("Start loading db video contents.");
	    try {
	      if(dbVideoName == null || dbVideoName.isEmpty()){
	    	  return;
	      }
	      //every query video in has 600 frames
	      dbImages = new ArrayList<BufferedImage>();
	      for(int i=1; i<=600; i++) {
	    	  String fileNum = "00";
	    	  if(i < 100 && i > 9) {
	    		  fileNum = "0";
	    	  } else if(i > 99) {
	    		  fileNum = "";
	    	  }
	    	  String fullName = dbFileFolder + "/" + dbVideoName + "/" + dbVideoName + fileNum + new Integer(i).toString() + ".rgb";
	    	  String audioFilename = dbFileFolder + "/" + dbVideoName + "/" + dbVideoName + ".wav";
	    	  
	    	  File file = new File(fullName);
	    	  InputStream is = new FileInputStream(file);

	   	      long len = file.length();
		      byte[] bytes = new byte[(int)len];
		      int offset = 0;
	          int numRead = 0;
	          while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	              offset += numRead;
	          }
	          System.out.println("Start loading frame: " + fullName);
	    	  int index = 0;
	          BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	          for (int y = 0; y < HEIGHT; y++) {
	            for (int x = 0; x < WIDTH; x++) {
	   				byte r = bytes[index];
	   				byte g = bytes[index+HEIGHT*WIDTH];
	   				byte b = bytes[index+HEIGHT*WIDTH*2]; 
	   				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
	    			image.setRGB(x,y,pix);
	    			index++;
	    		}
	    	  }
	          dbImages.add(image);
	          is.close();
	          playDBSound = new PlaySound(audioFilename);
	          System.out.println("End loading db frame: " + fullName);
	      }//end for
	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	      errorLabel.setText(e.getMessage());
	    } catch (IOException e) {
	      e.printStackTrace();
	      errorLabel.setText(e.getMessage());
	    }
	    this.resultPlayStatus = 3;
	    currentDBFrameNum = 0;
	    totalDBFrameNum = dbImages.size();
	    displayDBScreenShot();
	    System.out.println("End loading db video contents.");

		swingImagePanel.repaint();

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.playButton) {
			System.out.println("play button clicked");
			if(this.playStatus > 1) {
				this.playStatus = 1;
				this.playVideo();
			}
		} else if(e.getSource() == this.resultPlayButton) {
			System.out.println("result play button clicked");
			if(this.resultPlayStatus > 1) {
				this.resultPlayStatus = 1;
				this.playDBVideo();
			}
		} else if(e.getSource() == this.resultPauseButton) {
			System.out.println("result pause button clicked");
			if(this.resultPlayStatus == 1) {
				this.resultPlayStatus = 2;
				try {
					this.pauseDBVideo();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					errorLabel.setText(e1.getMessage());
					e1.printStackTrace();
				}
			}
		} else if(e.getSource() == this.pauseButton) {
			System.out.println("pause button clicked");
			if(this.playStatus == 1) {
				this.playStatus = 2;
				try {
					this.pauseVideo();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					errorLabel.setText(e1.getMessage());
				}
			}
		} else if(e.getSource() == this.stopButton) {
			System.out.println("stop button clicked");
			if(this.playStatus < 3) {
				this.playStatus = 3;
				this.stopVideo();
			}
		} else if(e.getSource() == this.resultStopButton) {
			System.out.println("result stop button clicked");
			if(this.resultPlayStatus < 3) {
				this.resultPlayStatus = 3;
				this.stopDBVideo();
			}
		}
		else if(e.getSource() == this.loadQueryButton) {
			String userInput = queryField.getText();
			if(userInput != null && !userInput.isEmpty()) {
				this.playingThread = null;
				this.audioThread = null;
				this.loadVideo(userInput.trim());

			}
		} else if(e.getSource() == this.searchButton){
			String userInput = queryField.getText();
			if(userInput.trim().isEmpty()) {
				return;
			}
			resultMap = searchClass.search(userInput.trim());
			resultListDisplay.removeAll();
		    resultListDisplay.add("Matched Videos:    ");
		    resultList = new ArrayList<Double>(7);
		    resultListRankedNames = new ArrayList<String>(7);
			sortedResultMap = new HashMap<String, Double>();
		    
		    Iterator it = resultMap.entrySet().iterator();
		    while (it.hasNext()) {
		        Entry pair = (Entry)it.next();
		        String videoName = (String)pair.getKey();
		        Double videoRank = new BigDecimal((Double)pair.getValue()).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
		        resultList.add(videoRank);
		        sortedResultMap.put(videoName, videoRank);
		    }
		    Collections.sort(resultList);
		    Collections.reverse(resultList);
		    for(int i=0; i<resultList.size(); i++) {
		    	Double tmpRank = resultList.get(i);
		    	it = sortedResultMap.entrySet().iterator();
			    while (it.hasNext()) {
			    	Entry pair = (Entry)it.next();
			    	Double videoRank = (Double)pair.getValue();
			    	if(videoRank == tmpRank) {
			    		resultListDisplay.add(pair.getKey() + "   " + (videoRank * 100) + "%");
			    		resultListRankedNames.add((String)pair.getKey());
			    		break;
			    	}
			    }
		    }
		    similarFrameMap = searchClass.framemap;
		} else if(e.getSource() == this.loadResultButton) {
			int userSelect = resultListDisplay.getSelectedIndex() - 1;
			if(userSelect > -1) {
				this.playingDBThread = null;
				this.audioDBThread = null;
				this.loadDBVideo(resultListRankedNames.get(userSelect));
				this.updateSimilarFrame();
			}
			slider.setMaximum(dbImages.size());

		}
	}
}
