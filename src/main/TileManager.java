package main;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class TileManager implements IEditableComponent{
	
	GamePanel gp;
	private int[][] tileGrid;
	BufferedImage[] bufferedImages;
	public final String TILE_SPRITESHEET_PATHA = "/images/tilesA.png";
	public final String TILE_SPRITESHEET_PATHB = "/images/tilesB.png";
	public final int DEFAULT_TILE_KIND = 0;
	private static final String DATA_FILE_SUFFIX = ".csv";
	private static final String DATA_FILE_PREFIX = "map";
	private boolean modified = false;

	public TileManager(GamePanel gp) {
		this.gp=gp;
		newTileGrid();
		try { initImages(); }catch(Exception e){e.printStackTrace();}
		
		gp.editor.addComponent(this);
	}
	
	public void newTileGrid() {
		System.out.println("Creating blank tile grid");
		this.tileGrid = new int[GamePanel.MAP_TILES_Y][GamePanel.MAP_TILES_X];
		for (int y = 0; y < GamePanel.MAP_TILES_Y;y++) {
			for (int x = 0; x < GamePanel.MAP_TILES_X;x++) {
				tileGrid[y][x]=DEFAULT_TILE_KIND;
			}
		}
	}
	

	
	public void initImages() throws IOException {
		//this.bufferedImages = new BufferedImage[20];
		Utils utils = new Utils();
		BufferedImage[] tilesA = utils.spriteSheetCutter(TILE_SPRITESHEET_PATHA,4,4,50,50);
		BufferedImage[] tilesB = utils.spriteSheetCutter(TILE_SPRITESHEET_PATHB,4,4,50,50);

		this.bufferedImages = Utils.appendArray(tilesA, tilesB);
	
	}
	/**
	 * 
	 * Which tiles are visible on screen
	 * 
	 * [top left corner x,
	 * top left corner y,
	 * bottom right corner x,
	 * bottom right corner y
	 * ]
	 * 
	 * @return
	 */
	public int[] getDrawableRange() {
		int[] ranges = {0,0,0,0};
		ranges[0] = GamePanel.wpScreenLocX / GamePanel.TILE_SIZE_PX;
		ranges[1] = GamePanel.wpScreenLocY / GamePanel.TILE_SIZE_PX;
		ranges[2] = (GamePanel.wpScreenLocX+GamePanel.WIDTH) / GamePanel.TILE_SIZE_PX;
		ranges[3] = (GamePanel.wpScreenLocY+GamePanel.HEIGHT) / GamePanel.TILE_SIZE_PX;
		return ranges;
		
	}
	
	private void renderTile(int screenX, int screenY, int kind) {

		gp.g2.drawImage(bufferedImages[kind], screenX, screenY, GamePanel.TILE_SIZE_PX, GamePanel.TILE_SIZE_PX,null);
		
	}
	
	private int clamp(int min, int max, int test) {
		if(test>max)return max;
		if(test<min)return min;
		return test;
	}
	
	public int getTileYX(int gridY, int gridX) {
		return tileGrid[gridY][gridX];
	}
	public int getTileXY(int gridX, int gridY) {
		return tileGrid[gridY][gridX];
	}
	
	public void draw() {
		
		int[] ranges = getDrawableRange();
		int startx = clamp(0,tileGrid[0].length,ranges[0]);
		int starty = clamp(0,tileGrid.length,ranges[1]);
		int endx = clamp(0,tileGrid[0].length,ranges[2]+1);
		int endy = clamp(0,tileGrid.length,ranges[3]+1);
		for (int y = starty; y < endy;y++) {
			for (int x = startx; x < endx;x++) {
				int worldX = x * GamePanel.TILE_SIZE_PX;
				int worldY = y * GamePanel.TILE_SIZE_PX;
				int screenX =  worldX - GamePanel.wpScreenLocX;
				int screenY =  worldY - GamePanel.wpScreenLocY;
				int kind = tileGrid[y][x];
				renderTile(screenX,screenY,kind);
			}
		}
		
	}
	
	public void highlightTile(int tileX, int tileY, Color color) {
		
		if(gp.g2==null)return;
		int screenX = tileX*gp.TILE_SIZE_PX - GamePanel.wpScreenLocX;
		int screenY = tileY*gp.TILE_SIZE_PX - GamePanel.wpScreenLocY;
		gp.g2.setColor(color);
		gp.g2.fillRect(
				screenX, 
				screenY, 
				gp.TILE_SIZE_PX , 
				gp.TILE_SIZE_PX );
		
	}
	
	public void update() {
		
	}

	@Override
	public boolean validateAssetID(int testAssetID) {
		int maximum = this.maxAssetID();
		int actualAssetID = testAssetID;
		if (testAssetID > maximum) {
			testAssetID = 0;
		}else if(testAssetID <  0) {
			testAssetID = maximum;
		}else {
			actualAssetID = testAssetID;
		}
		try {
			BufferedImage asset = this.bufferedImages[actualAssetID];
		}catch(Exception e) {
			return false;
		}
		
		
		return true;
	}

	@Override
	public void paintAsset(int gridX, int gridY, int kind) {
		modified=true;
		try {
			this.tileGrid[gridY][gridX] = kind;
		}
		catch(Exception e){
			e.printStackTrace();
		} 
	
	}

	@Override
	public int maxAssetID() {
		
		return this.bufferedImages.length;
	}

	
	@Override
	public String getDataFilename() {
		return Utils.getLevelresourceFilename(this.gp.level, TileManager.DATA_FILE_PREFIX, TileManager.DATA_FILE_SUFFIX);
	}

	@Override
	public EditMode getEditMode() {
		return EditMode.TILE;
	}

	@Override
	public int[][] getGridData() {
		return this.tileGrid;
	}

	@Override
	public void setGridData(int[][] data) {
		this.tileGrid = data;
		
	}
	@Override
	public boolean isModified() {
		if (modified) {
			modified=false;
			return true;
		}
		return false;
	}

}
