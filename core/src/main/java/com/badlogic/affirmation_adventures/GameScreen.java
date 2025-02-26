package com.badlogic.affirmation_adventures;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Represents the game screen where the main gameplay occurs.
 */

public class GameScreen implements Screen {
    final affirmation_adventures game;

    public Texture playerTexture;
    public Sprite playerSprite;
    public OrthogonalTiledMapRenderer mapRenderer;
    public OrthographicCamera camera = new OrthographicCamera();
    public TiledMap tiledMap;
    // private AssetManager assetManager;

    public Rectangle playerBounds;

    /**
     * Constructs a new GameScreen.
     *
     * @param game the main game instance
     */

    public GameScreen(final affirmation_adventures game) {
        this.game = game;

        /*
        assetManager = new AssetManager();
        assetManager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));

         */

        try {
            playerTexture = new Texture("player.png");
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Failed to load player texture", e);
        }

        if (playerTexture != null) {
            playerSprite = new Sprite(playerTexture);
            playerSprite.setSize(1, 1);
            playerBounds = new Rectangle(playerSprite.getX(), playerSprite.getY(), playerSprite.getWidth(), playerSprite.getHeight());
        }

        try {
            tiledMap = new TmxMapLoader().load("Dungeon.tmx");
            if (tiledMap == null) {
                Gdx.app.error("GameScreen", "Failed to load map: Dungeon.tmx");
            } else {
                Gdx.app.log("GameScreen", "Map loaded successfully");
                mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1 / 16f);
                if (mapRenderer == null) {
                    Gdx.app.error("GameScreen", "Failed to initialize map renderer");
                } else {
                    Gdx.app.log("GameScreen", "MapRenderer created successfully");
                }
            }
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Exception loading map", e);
        }

        /*
        try {
            Gdx.app.log("GameScreen", "Loading map: Dungeon.tmx");
           assetManager.load("Dungeon.tmx", TiledMap.class);
           assetManager.finishLoading();
           map = assetManager.get("Dungeon.tmx", TiledMap.class);
            if (map == null) {
                Gdx.app.error("GameScreen", "Map is null after loading");
            }
            else {
                Gdx.app.log("GameScreen", "Map loaded successfully");
                mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / 16f);
            }
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Failed to load map", e);
        }
        */

        if (tiledMap != null) {
            int mapWidth = tiledMap.getProperties().get("width", Integer.class);
            int mapHeight = tiledMap.getProperties().get("height", Integer.class);
            int tilePixelWidth = tiledMap.getProperties().get("tilewidth", Integer.class);
            int tilePixelHeight = tiledMap.getProperties().get("tileheight", Integer.class);
            float centerX = (mapWidth * tilePixelWidth) / 2f;
            float centerY = (mapHeight * tilePixelHeight) / 2f;

            camera.position.set(centerX, centerY, 0);
            camera.setToOrtho(false, 100, 100);
            camera.update();
        }
    }

    @Override
    public void show() {
        // Prepare your screen here.
    }

    @Override
    public void render(float delta) {
        input();
        logic();
        draw();

    }

    /**
     * Handles the drawing of the game screen.
     */

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        if (camera != null && playerSprite != null) {
            camera.position.set(playerSprite.getX(), playerSprite.getY(), 0);
            camera.update();
        }

        if (mapRenderer != null) {
            mapRenderer.setView(camera);
            mapRenderer.render();
        } else {
            Gdx.app.error("GameScreen", "MapRenderer is null, cannot render map");
        }

        if (game.batch != null && camera != null) {
            game.batch.setProjectionMatrix(camera.combined);
            game.batch.begin();
            if (playerSprite != null) {
                playerSprite.draw(game.batch);
            }
            game.batch.end();
        }
    }

    /**
     * Handles the game logic.
     */

    private void logic() {
        if (playerSprite != null)
            playerBounds.setPosition(playerSprite.getX(), playerSprite.getY());
    }

    /**
     * Handles the user input.
     */

    private void input() {
        float speed = 20f;
        float delta = Gdx.graphics.getDeltaTime();

        float moveX = 0;
        float moveY = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveX = speed * delta;
        }

        else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveX = -speed * delta;
        }

        else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            moveY = speed * delta;
        }

        else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            moveY = -speed * delta;
        }

        boolean collision = false;

        MapObjects objects= null;
        if (tiledMap != null && tiledMap.getLayers().get("Torches and Treasures") != null) {
            objects = tiledMap.getLayers().get("Torches and Treasures").getObjects();
        }
        if (objects !=null) {
            for (MapObject object : objects) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    if (object.getProperties().containsKey("collidable") && (boolean) object.getProperties().get("collidable")) {
                        if (playerBounds.overlaps(rect)) {
                            collision = true;
                            break;
                        }
                    }
                }
            }
        }

        if (playerSprite != null) {
            playerSprite.translate(moveX, moveY);
            playerBounds.setPosition(playerSprite.getX(), playerSprite.getY());
        }

    }



    @Override
    public void resize(int width, int height) {
        if (camera != null) {
            camera.setToOrtho(false, 100, 100);
            camera.update();
        }

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        if (playerTexture != null) {
            playerTexture.dispose();
        }
        if (mapRenderer != null) {
            mapRenderer.dispose();
        }
        if (tiledMap != null) {
            tiledMap.dispose();
        }
        /*
        if (assetManager != null) {
            assetManager.dispose();
        }

         */
    }
}
