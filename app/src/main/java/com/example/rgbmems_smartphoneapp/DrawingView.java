package com.example.rgbmems_smartphoneapp;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.example.rgbmems_smartphoneapp.databinding.FragmentSecondBinding;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

public class DrawingView extends View {
    // Global Variables
    private Path drawPath; // Path for drawing
    private Paint drawPaint, erasePaint, canvasPaint; // Paint objects for drawing, erasing, and canvas
    private Bitmap canvasBitmap; // Bitmap for the canvas
    private Canvas drawCanvas; // Canvas to draw on the bitmap
    private float brushSize = 10;  // Size for drawing brush
    private float eraserSize = 50; // Size for eraser
    private static final Stack<Bitmap> undoStack = new Stack<>(); // Stack for undo operations
    private static final Stack<Bitmap> redoStack = new Stack<>(); // Stack for redo operations
    // Initialize the ToolMode variable to the neutral state of the tool, indicating no active operation.
    private int ToolMode = SecondFragment.TOOL_NEUTRAL;
    private Bitmap backgroundBitmap; // Bitmap for background image

    // Initialization
    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    /** 
	*Setup drawing properties
	*/
	
    private void setupDrawing() {
        // Setup draw paint for drawing
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(Color.BLACK);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        // Setup erase paint for erasing
        erasePaint = new Paint();
        erasePaint.setAntiAlias(true);
        erasePaint.setStrokeWidth(brushSize);
        erasePaint.setStyle(Paint.Style.FILL);
        erasePaint.setStrokeJoin(Paint.Join.ROUND);
        erasePaint.setStrokeCap(Paint.Cap.ROUND);
        erasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvasPaint = new Paint(Paint.DITHER_FLAG);

    }
    /**
     * This method creates a Bitmap representation of the current drawing view.
     * It initializes a Bitmap object with the same dimensions as the view,
     * draws the current contents onto a Canvas using the draw() method,
     * and returns the resulting Bitmap.
     */
	
    public Bitmap getBitmapFromDrawingView() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return bitmap;

    }
	
    /**
     * This method is called when the size of the view changes.
     * It initializes a new Bitmap and Canvas with the updated dimensions (width and height).
     * This allows for redrawing the view on the new canvas when the size changes.
     */
	
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);

    }
	
    /**
     * Sets the paint color used for drawing.
     * This method updates the color of the drawPaint object
     * and calls invalidate() to refresh the view, ensuring
     * that the changes take effect visually.
     *
     * @param color The new color to set for the paint.
     */
	
    public void setPaintColor(int color) {
        drawPaint.setColor(color);
        invalidate();

    }
	
    /**
     * This method is called to draw the view's content on the provided Canvas.
     * It handles drawing a background bitmap (if available), the canvas bitmap,
     * and paths based on the current drawing tool mode.
     * If a background bitmap is set, it scales and centers the bitmap within the view.
     * It then draws the canvas bitmap and applies the appropriate paint based on the
     * selected tool mode:
     * - Eraser: Draws the path using the erasePaint.
     * - Neutral: No drawing is performed.
     * - Other tools: Draws the path using the drawPaint.
     *
     * @param canvas The Canvas on which to draw the view's content.
     */
	
    @Override
    protected void onDraw(Canvas canvas) {
        if (backgroundBitmap != null) {
            int bitmapWidth = backgroundBitmap.getWidth();
            int bitmapHeight = backgroundBitmap.getHeight();
            float viewWidth = getWidth();
            float viewHeight = getHeight();
            float scale = Math.min(viewWidth / bitmapWidth, viewHeight / bitmapHeight);
            int newWidth = (int) (bitmapWidth * scale);
            int newHeight = (int) (bitmapHeight * scale);
            int left = (int) ((viewWidth - newWidth) / 2);
            int top = (int) ((viewHeight - newHeight) / 2);
            canvas.drawBitmap(backgroundBitmap, null, new Rect(left, top, left + newWidth, top + newHeight), null);
        }
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        if (ToolMode == SecondFragment.TOOL_ERASER){            // 消しゴムの場合
            canvas.drawPath(drawPath, erasePaint);
        } else if (ToolMode == SecondFragment.TOOL_NEUTRAL) {    // ニュートラル状態の場合
        // 描画ツールがactiveではないため設定しない。
        } else {                        // 消しゴム以外の描画ツールの場合
            canvas.drawPath(drawPath, drawPaint);
        }
    }

    /**
     * Handles touch events for drawing on the canvas.
     * This method processes different touch actions (down, move, up)
     * and executes the appropriate drawing operations based on the
     * current tool mode.
     * - On ACTION_DOWN: Initializes drawing based on the selected tool:
     *   - Neutral: No action taken.
     *   - Eraser: Saves the current canvas state and draws a circle at the touch point.
     *   - Black Pen: Saves the current canvas state and moves the path to the touch point.
     * - On ACTION_MOVE: Continues drawing based on the selected tool:
     *   - Neutral: No action taken.
     *   - Eraser: Draws a circle at the current touch position.
     *   - Black Pen: Draws a line to the current touch position.
     * - On ACTION_UP: Finalizes the drawing:
     *   - Neutral: No action taken.
     *   - Eraser: Draws a circle at the final touch position.
     *   - Black Pen: Draws a line to the final touch position and completes the path.
     * The method then calls invalidate() to refresh the view.
     *
     * @param event The MotionEvent object containing touch event details.
     * @return True if the event is handled, false otherwise.
     */
	
    @Override
        public boolean onTouchEvent(MotionEvent event) {
            float touchX = event.getX(); // Get touch X coordinate
            float touchY = event.getY(); // Get touch Y coordinate
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (ToolMode == SecondFragment.TOOL_NEUTRAL) {
                        //何もしない
                    } else if (ToolMode == SecondFragment.TOOL_ERASER) {
                        saveCanvasState();
                        drawCanvas.drawCircle(touchX, touchY, eraserSize, erasePaint);
                    } else if (ToolMode == SecondFragment.TOOL_BLACK_PEN) {
                        saveCanvasState();
                        drawPath.moveTo(touchX, touchY);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (ToolMode == SecondFragment.TOOL_NEUTRAL) {
                        // 何もしない
                    }
                    else if (ToolMode == SecondFragment.TOOL_ERASER) {
                        drawCanvas.drawCircle(touchX, touchY, eraserSize, erasePaint);
                    }
                    else if (ToolMode == SecondFragment.TOOL_BLACK_PEN) {
                        drawPath.lineTo(touchX, touchY);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (ToolMode == SecondFragment.TOOL_NEUTRAL) {
                        // 何もしない
                    }
                    else if (ToolMode == SecondFragment.TOOL_ERASER) {
                        drawCanvas.drawCircle(touchX, touchY, eraserSize, erasePaint);
                    }
                    else if (ToolMode == SecondFragment.TOOL_BLACK_PEN) {
                        drawPath.lineTo(touchX, touchY);
                        drawCanvas.drawPath(drawPath, drawPaint);
                    }
                    drawPath.reset();
                    break;
                default:
                    return false;
            }
            invalidate();
            return true;
        }

    /**
     * Saves the current state of the canvas by creating a copy of the canvas bitmap.
     * Clears the redo stack to prepare for new drawing operations.
     */
    public void saveCanvasState() {
        Bitmap saveBitmap = canvasBitmap.copy(Bitmap.Config.ARGB_8888, true);
        undoStack.push(saveBitmap);
        redoStack.clear();
    }

    /**
     * Reverts the last drawing action by restoring the previous canvas state.
     * Saves the current canvas state to the redo stack before updating.
     * Calls invalidate() to refresh the view after the undo operation.
     */
    public void undo() {
        if (!   undoStack.isEmpty()) {
            redoStack.push(canvasBitmap.copy(Bitmap.Config.ARGB_8888, true));
            canvasBitmap = undoStack.pop();
            drawCanvas.setBitmap(canvasBitmap);
            invalidate();
        }
    }

    /**
     * Restores the last undone drawing action by retrieving the canvas state
     * from the redo stack and updating the current canvas.
     */
	
    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(canvasBitmap.copy(Bitmap.Config.ARGB_8888, true));
            canvasBitmap = redoStack.pop();
            drawCanvas.setBitmap(canvasBitmap);
            invalidate();
        }
    }

    /**
     * Sets the current tool mode for drawing operations.
     *
     * @param isToolMode The tool mode to be set.
     */
	
    public void setToolMode(int isToolMode) {
        ToolMode = isToolMode;
    }

    /**
     * Sets the thickness of the brush for drawing and erasing.
     *
     * @param thickness The new thickness value for the brush.
     */
	
    public void setBrushThickness(float thickness) {
        this.brushSize = thickness;
        drawPaint.setStrokeWidth(thickness);
        erasePaint.setStrokeWidth(thickness);
        invalidate();
    }

    /**
     * Sets the background bitmap for the drawing view and refreshes the view.
     *
     * @param bitmap The bitmap to be set as the background.
     */
	
    public void setBackgroundBitmap(Bitmap bitmap) {
        this.backgroundBitmap = bitmap;
        invalidate();
    }

    /**
     * Loads an image from the specified URI, resizes it to fit the view,
     * and sets it as the background bitmap.
     *
     * @param imageUri The URI of the image to be loaded.
     */
	
    public void loadImage(Uri imageUri) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            int viewWidth = getWidth();
            int viewHeight = getHeight();
            float imageRatio = (float) bitmap.getWidth() / bitmap.getHeight();
            float viewRatio = (float) viewWidth / viewHeight;
            int newWidth, newHeight;
            if (imageRatio > viewRatio) {
                newWidth = viewWidth;
                newHeight = Math.round(viewWidth / imageRatio);
            }
            else {
                newHeight = viewHeight;
                newWidth = Math.round(viewHeight * imageRatio);
            }
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            setBackgroundBitmap(resizedBitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clears the drawing canvas and removes the background bitmap.
     * Resets the canvas to a transparent state.
     */
	
    public void clear() {
        if (drawCanvas != null) {
            drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            backgroundBitmap = null;
            invalidate();
        }
    }

    /**
     *  Clears all items in the undo and redo stacks
     */
	
    public void resetUndoRedoStacks() {
        undoStack.clear();
        redoStack.clear();
    }

    /**
     * Creates and returns a bitmap that combines the background bitmap
     * and the current canvas strokes.
     *
     * @return A bitmap representing the combined background and drawing.
     */
	
    public Bitmap getBitmap() {
        Bitmap combinedBitmap = Bitmap.createBitmap(canvasBitmap.getWidth(), canvasBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas combinedCanvas = new Canvas(combinedBitmap);
        if (backgroundBitmap != null) {
            combinedCanvas.drawBitmap(backgroundBitmap, 0, 0, null);
        }
        combinedCanvas.drawBitmap(canvasBitmap, 0, 0, null);
        return combinedBitmap; // Return a bitmap that contains both the background and the strokes
    }

    /**
     * Saves the current drawing as a PNG image in the Pictures/MyApp directory.
     *
     * @param context The context used to access the content resolver and save the image.
     */
	
    public void saveImage(Context context) {
        Bitmap bitmap = getBitmap();
        String filename = "Drawing_" + System.currentTimeMillis() + ".png";
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyApp");
            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (FileOutputStream outputStream = (FileOutputStream) context.getContentResolver().openOutputStream(uri)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            bitmap.recycle();
        }
    }
}