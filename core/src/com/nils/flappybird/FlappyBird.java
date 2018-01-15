package com.nils.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter {
	SpriteBatch batch;
	Texture background;
	Texture toptube;
	Texture bottupe;
	Texture gameover;
	Texture[] birds;
	int flapState = 0;
	int counter = 0;
	int scoringTube = 0;
	BitmapFont font;

	float birdY = 0;
	float velocity = 0;
	Circle birdCircle;
//	ShapeRenderer shapeRenderer;

	int gameState = 0;
	float gravity = 1;
	float gap = 400;
	float maxTubeOffset;
	float tubeVelocity = 4;
	int numberOfTubes = 4;
	float[] tubeX = new float[numberOfTubes];
	float[] tubeOffset = new float[numberOfTubes];
	float distanceBetweenTubes;

	int score;

	Rectangle[] topTubeRects = new Rectangle[numberOfTubes];
	Rectangle[] botTubeRects = new Rectangle[numberOfTubes];

	Random rand;

	private final static int COUNTER_MAX = 16;
	private final static int TUBE_OFFSET_EXTRA = 300;
	private final static int BIRD_RADIUS_SUBTRACT = 20;

	@Override
	public void create () {
		batch = new SpriteBatch();
		background = new Texture("bg.png");
		birds = new Texture[2];
		birds[0] = new Texture("bird.png");
		birds[1] = new Texture("bird2.png");

		toptube = new Texture("toptube.png");
		bottupe = new Texture("bottomtube.png");
		gameover = new Texture("gameover.png");

		birdCircle = new Circle();
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		font.getData().setScale(10);
//		shapeRenderer = new ShapeRenderer();

		score = 0;

		maxTubeOffset = Gdx.graphics.getHeight() / 2 - gap / 2 - TUBE_OFFSET_EXTRA;
		rand = new Random();

		distanceBetweenTubes = Gdx.graphics.getWidth() * 3 / 4;

		startGame();

	}

	@Override
	public void render () {

		batch.begin();
		batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		// if bird is falling
		if (gameState == 1) {

			// scoring
			if (tubeX[scoringTube] < Gdx.graphics.getWidth() / 2 - toptube.getWidth() / 2) {
				score++;

				if (scoringTube < numberOfTubes - 1) {
					scoringTube++;
				} else {
					scoringTube = 0;
				}
			}

			// jump up
			if (Gdx.input.justTouched()) {
				velocity = -20;
			}

			for (int i = 0; i < numberOfTubes; i++) {

				if (tubeX[i] < -toptube.getWidth()) {
					tubeX[i] += numberOfTubes * distanceBetweenTubes;
					tubeOffset[i] = (rand.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - gap - TUBE_OFFSET_EXTRA * 2);
				} else {
					tubeX[i] -= tubeVelocity;
				}



				batch.draw(toptube, tubeX[i], Gdx.graphics.getHeight() / 2 + gap / 2 + tubeOffset[i]);
				batch.draw(bottupe, tubeX[i], Gdx.graphics.getHeight() / 2 - gap / 2 - bottupe.getHeight() + tubeOffset[i]);
			}

			if (birdY > 0 && birdY < Gdx.graphics.getHeight()) {
				// gravity effect
				velocity += gravity;
				birdY -= velocity;
			} else {
				gameState = 2;
			}

			// if bird is stationary (game has not begun yet)
		} else if (gameState == 0) {
			if (Gdx.input.justTouched()) {
				gameState = 1;
			}
			// if game is over
		} else {
			batch.draw(gameover, Gdx.graphics.getWidth() / 2 - gameover.getWidth() / 2, Gdx.graphics.getHeight() / 2 - gameover.getHeight() / 2);
			if (Gdx.input.justTouched()) {
				gameState = 1;
				startGame();
				score = 0;
				scoringTube = 0;
				velocity = 0;
			}
		}

		// add flapping effect to bird
		if (counter == COUNTER_MAX) {
			flapState = 1;
			counter = 0;
		} else if (counter < COUNTER_MAX / 2) {
			flapState = 0;
			counter++;
		} else {
			flapState = 1;
			counter++;
		}

		// draw bird
		batch.draw(birds[flapState],
				Gdx.graphics.getWidth() / 2 - birds[flapState].getWidth() / 2,
				birdY);
		font.draw(batch, Integer.toString(score), Gdx.graphics.getWidth() / 2 - font.getSpaceWidth() / 2, Gdx.graphics.getHeight() - 200);
		batch.end();

		birdCircle.set(Gdx.graphics.getWidth() / 2, birdY + birds[flapState].getHeight() / 2, birds[flapState].getWidth() / 2 - BIRD_RADIUS_SUBTRACT);

//		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//		shapeRenderer.setColor(Color.RED);
//		shapeRenderer.circle(birdCircle.x, birdCircle.y, birdCircle.radius);
		for (int i = 0; i < numberOfTubes; i++) {
			topTubeRects[i] = new Rectangle(tubeX[i], Gdx.graphics.getHeight() / 2 + gap / 2 + tubeOffset[i], toptube.getWidth(), toptube.getHeight());
			botTubeRects[i] = new Rectangle(tubeX[i], Gdx.graphics.getHeight() / 2 - gap / 2 - bottupe.getHeight() + tubeOffset[i], bottupe.getWidth(), bottupe.getHeight());
//			shapeRenderer.rect(topTubeRects[i].x, topTubeRects[i].y, topTubeRects[i].width, topTubeRects[i].height);
//			shapeRenderer.rect(botTubeRects[i].x, botTubeRects[i].y, botTubeRects[i].width, botTubeRects[i].height);

			if (Intersector.overlaps(birdCircle, topTubeRects[i]) || Intersector.overlaps(birdCircle, botTubeRects[i])) {
				gameState = 2;
			}
		}
		Gdx.app.log("Score: ", Integer.toString(score));
//		shapeRenderer.end();
	}

	private void startGame() {
		birdY = Gdx.graphics.getHeight() / 2 - birds[0].getHeight() / 2;

		for (int i = 0; i < numberOfTubes; i++) {
			tubeOffset[i] = (rand.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - gap - TUBE_OFFSET_EXTRA * 2);
			tubeX[i] = Gdx.graphics.getWidth() / 2 - toptube.getWidth() / 2 + Gdx.graphics.getWidth() + i * distanceBetweenTubes;
		}
	}

	@Override
	public void dispose () {
		batch.dispose();
		background.dispose();
	}
}
