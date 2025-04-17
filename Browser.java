import javafx.application.Application;
import javafx.stage.Stage;

import javafx.scene.Scene;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;

import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.Modality;
import javafx.scene.Node;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Date;
import java.text.SimpleDateFormat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Scanner;

// For MOTD and animations
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Ellipse;
import javafx.animation.TranslateTransition;
import javafx.animation.RotateTransition;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.Glow;
import javafx.scene.effect.Reflection;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Font;

// For Game
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.shape.Polygon;

class Post {
    private String auth;
    private int postNum;
    private String txtBody;
    private int likes;

    Post(String author, int number, String body) {
        this.auth = author;
        this.postNum = number;
        this.txtBody = body;
        this.likes = 0;
    }
    // Another constructor that takes into consideration the likes thing.
    Post(String author, int number, String body, int likes) {
        this.auth = author;
        this.postNum = number;
        this.txtBody = body;
        this.likes = likes;
    }

    public String getAuth() {
        return auth;
    }

    public int getPostNum() {
        return postNum;
    }

    public String getTxtBody() {
        return txtBody;
    }

    public int getLikes() {
        return likes;
    }

    public void incrementLikes() {
        this.likes++;
    }


    @Override
    public String toString() {
        return auth + "|" + postNum + "|" + txtBody + "|" + likes;
    }

    public static Post fromString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 4) {
            return new Post(parts[0], Integer.parseInt(parts[1]), parts[2], Integer.parseInt(parts[3]));
        } else {
            return new Post(parts[0], Integer.parseInt(parts[1]), parts[2], 0);
        }
    }
}

/**
 * @author Raj Kothari
 * @version 1.0
 * Browser class implementation for a simple web browser.
 */
public class Browser extends Application {
    private Stage primaryStage; // -> The main stage
    private VBox postArea; //-> Store postArea to access in stop()
    private BorderPane primaryLayout;
    private List<String> history = new ArrayList<>();
    private int historyIndex = -1;
    private boolean sortNewestFirst = true; // Default: newest first (current behavior)
    private TextField urlBarField; // Add a class-level field for the URL bar

    // ***************************** //
    // Named Inner Class //
    // ***************************** //
    private class GoButtonHandler implements EventHandler<ActionEvent> {
        private TextField urlBarField;
        private Text displayTxt1;
        private Text displaytxt2;


        GoButtonHandler(TextField urlBarField, Text displayText1, Text displayText2) {
            this.urlBarField = urlBarField;
            this.displayTxt1 = displayText1;
            this.displaytxt2 = displayText2;
        }

        @Override
        public void handle(ActionEvent event) {
            String url = urlBarField.getText().trim();
            handleUrlSubmission(url, displayTxt1, displaytxt2);
        }
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        primaryLayout = new BorderPane();

        urlBarField = new TextField(); // Store in class field instead of local variable
        urlBarField.setPromptText("Type a URL...");
        urlBarField.setPrefWidth(400);

        Button goButton = new Button("Go");
        Button backButton = new Button("Back");
        Button forwardButton = new Button("Forward");

        // Initially disabled both
        backButton.setDisable(true);
        forwardButton.setDisable(true);

        HBox urlBarButton = new HBox(5); // Note: 5 here for spacing between child elements
        urlBarButton.setAlignment(Pos.CENTER);
        urlBarButton.setPadding(new Insets(5));
        urlBarButton.setStyle("-fx-background-color: rgb(164, 172, 172);");
        urlBarButton.getChildren().addAll(backButton, forwardButton, urlBarField, goButton);

        primaryLayout.setTop(urlBarButton);

        // Welcome page texts
        Text displayText1 = new Text("Welcome!");
        Text displayText2 = new Text("Type a URL in the URL bar to get started");
        displayText1.setStyle("-fx-font-size: 12px;");
        displayText2.setStyle("-fx-font-size: 12px;");

        // Set welcome page as initial view
        VBox textContainer = createWelcomeView(displayText1, displayText2);
        primaryLayout.setCenter(textContainer);
        BorderPane.setAlignment(textContainer, Pos.CENTER);

        // Scene - like the window
        Scene scene = new Scene(primaryLayout, 600, 400); // 600 x 400 px
        primaryStage.setTitle("Internet Explorer");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Go button action
        goButton.setOnAction(event -> {
            String url = urlBarField.getText().trim();
            if (!url.isEmpty()) {
                navToUrl(url, displayText1, displayText2, backButton, forwardButton);
            }
        });

        // Back button action
        backButton.setOnAction(event -> {
            if (historyIndex > 0) {
                historyIndex--;
                String url = history.get(historyIndex);
                urlBarField.setText(url);
                handleUrlSubmission(url, displayText1, displayText2);
                updateButtonStates(backButton, forwardButton);
                System.out.println("Navigating back to: " + url); // Print to terminal
            }
        });

        // Forward button action
        forwardButton.setOnAction(event -> {
            if (historyIndex < history.size() - 1) {
                historyIndex++;
                String url = history.get(historyIndex);
                urlBarField.setText(url);
                handleUrlSubmission(url, displayText1, displayText2);
                updateButtonStates(backButton, forwardButton);
                System.out.println("Navigating forward to: " + url); // Print to terminal
            }
        });

        // URL bar Enter key action
        urlBarField.setOnAction(event -> {
            String url = urlBarField.getText().trim();
            if (!url.isEmpty()) {
                navToUrl(url, displayText1, displayText2, backButton, forwardButton);
            }
        });
        
        // Set initial title
        updateTitle("Welcome Page");
    }

    // Helper method to navigate to a URL and update history
    private void navToUrl(String url, Text displayText1, Text displayText2, Button backButton, Button forwardButton) {
        // Trim the URL before processing
        url = url.trim();
        
        // If navigating to a new URL, remove forward history
        if (historyIndex < history.size() - 1) {
            history = history.subList(0, historyIndex + 1);
        }
        
        // Add to history only if different from current page
        if (historyIndex < 0 || !history.get(historyIndex).equalsIgnoreCase(url)) {
            history.add(url);
            historyIndex++;
            handleUrlSubmission(url, displayText1, displayText2);
            updateButtonStates(backButton, forwardButton);
        } else {
            // Just refresh the current page
            handleUrlSubmission(url, displayText1, displayText2);
        }
    }

    // Helper method to update Back and Forward button states
    private void updateButtonStates(Button backButton, Button forwardButton) {
        backButton.setDisable(historyIndex <= 0);
        forwardButton.setDisable(historyIndex >= history.size() - 1);
    }

    // Helper method => processes URL submissions
    private void handleUrlSubmission(String url, Text displayText1, Text displayText2) {
        // Trim and convert URL to lowercase for case-insensitive comparison
        String lowerUrl = url.trim().toLowerCase();
        
        // If it's "home", show the welcome page
        if (lowerUrl.equals("home")) {
            updateTitle("Home");
            VBox welcomeContainer = createWelcomeView(displayText1, displayText2);
            primaryLayout.setCenter(welcomeContainer);
            BorderPane.setAlignment(welcomeContainer, Pos.CENTER);
        //if it's the Java Discussion mock page
        } else if (lowerUrl.equals("javadiscussion.com")) {
            updateTitle("Java Discussion");
            VBox forumLayout = new VBox(10);

            // Header with "Java Discussion"
            VBox mainHeader = new VBox(10);
            mainHeader.setStyle("-fx-background-color: purple;");
            mainHeader.setPadding(new Insets(5));
            mainHeader.setAlignment(Pos.CENTER); // Center the content in the VBox
            Text headerText = new Text("Java Discussion");
            headerText.setStyle("-fx-font-size: 20px; -fx-fill: white;");

            Button newPostButton = new Button("New Post");
            Button sortButton = new Button("Sort: Newest First"); // Initial text

            sortButton.setOnAction(event -> {
                sortNewestFirst = !sortNewestFirst; // Toggle sort order
                sortButton.setText("Sort: " + (sortNewestFirst ? "Newest First" : "Oldest First"));
                loadPosts(postArea); // Reload posts with new sort order
                System.out.println("Sorting posts: " + (sortNewestFirst ? "Newest First" : "Oldest First"));
            });

            mainHeader.getChildren().addAll(headerText, newPostButton, sortButton);
            forumLayout.getChildren().add(mainHeader);

            // Scrolly stuff
            postArea = new VBox(10);
            postArea.setPadding(new Insets(10));
            ScrollPane scrollPane = new ScrollPane(postArea);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            scrollPane.setFitToWidth(true);
            forumLayout.getChildren().add(scrollPane);

            // Load existing posts
            loadPosts(postArea);

            primaryLayout.setCenter(forumLayout);
            BorderPane.setAlignment(forumLayout, Pos.CENTER);

            // Handle New Post
            // ***************************** //
            // Anonymous Inner Class //
            // ***************************** //
            newPostButton.setOnAction(new EventHandler<ActionEvent>() { // Anonymous inner class
                @Override
                public void handle(ActionEvent event) {
                    showNewPostWindow(postArea);
                }
            });
        } else if (lowerUrl.equals("1331motd.com")) {
            updateTitle("Message of the Day");
            // Pane for MOTD graphic
            Pane motdPane = new Pane();
            motdPane.setStyle("-fx-background-color: rgb(173, 216, 230);"); // lightblue in RGB

            // Quotes Credit: Google :)
            String[] quotes = {
                "Java: write once, run away!",
                "Why do Java developers wear glasses? Because they can't C#!",
                "Real programmers count from 0.",
                "Why was the Java developer always broke? Because he lost his class!",
                "Java: write once, debug everywhere."
            };

            // Random quote is selected from the array above
            String randomQuote = quotes[(int) (Math.random() * quotes.length)];

            // Message of the day
            Text motdText = new Text(randomQuote);
            motdText.setStyle("-fx-font-size: 16px; -fx-fill: rgb(0, 0, 0);");
            motdText.setX(200);
            motdText.setY(50);
            motdText.setWrappingWidth(200);
            motdText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            motdPane.getChildren().add(motdText);

            // Coffee cup base (rectangle)
            Rectangle cupBase = new Rectangle(200, 200, 200, 150);
            cupBase.setFill(Color.rgb(245, 245, 220)); // beige in RGB
            motdPane.getChildren().add(cupBase);

            // Coffee inside (ellipse)
            Ellipse coffee = new Ellipse(300, 200, 90, 40);
            coffee.setFill(Color.rgb(165, 42, 42)); // brown in RGB
            motdPane.getChildren().add(coffee);

            // Handle (circle)
            Circle handle = new Circle(420, 275, 40); // centerX, centerY, radius
            handle.setFill(Color.TRANSPARENT);
            handle.setStroke(Color.rgb(245, 245, 220)); // beige in RGB
            handle.setStrokeWidth(15);
            motdPane.getChildren().add(handle);

            // Steam (lines)
            Line steam1 = new Line(250, 150, 250, 100); // startX, startY, endX, endY
            steam1.setStroke(Color.rgb(128, 128, 128)); // gray in RGB
            motdPane.getChildren().add(steam1);

            Line steam2 = new Line(300, 140, 300, 90);
            steam2.setStroke(Color.rgb(128, 128, 128)); // gray in RGB
            motdPane.getChildren().add(steam2);

            Line steam3 = new Line(350, 150, 350, 100);
            steam3.setStroke(Color.rgb(128, 128, 128)); // gray in RGB
            motdPane.getChildren().add(steam3);

            // Set the motdPane as the center of the primary layout
            primaryLayout.setCenter(motdPane);
        } else if (lowerUrl.equals("aboutbrowser")) {
            updateTitle("About Browser");
            // Create an about page for the browser
            VBox aboutContainer = new VBox(15);
            aboutContainer.setAlignment(Pos.CENTER);
            aboutContainer.setPadding(new Insets(20));
            
            Text aboutTitle = new Text("About Mock Browser");
            aboutTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
            
            Text aboutDescription = new Text(
                "This is a mock browser application created for CS 1331.\n" +
                "It demonstrates various Java GUI capabilities using JavaFX.\n\n" +
                "This browser does not connect to the actual internet."
            );
            aboutDescription.setStyle("-fx-font-size: 14px;");
            aboutDescription.setWrappingWidth(400);
            aboutDescription.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            
            Text availablePages = new Text(
                "Available mock pages:\n" +
                "• home - Welcome page\n" +
                "• javadiscussion.com - Java forum\n" +
                "• 1331motd.com - Message of the day"
            );
            availablePages.setStyle("-fx-font-size: 14px;");
            
            aboutContainer.getChildren().addAll(aboutTitle, aboutDescription, availablePages);
            primaryLayout.setCenter(aboutContainer);
        } else {
            // Apply trim before adding to history to ensure consistency
            if (urlBarField != null) {  // Check if urlBarField is not null
                urlBarField.setText(lowerUrl);
            }
            
            // Display an informative error for invalid URLs
            Alert errorAlert = new Alert(AlertType.INFORMATION);
            errorAlert.setTitle("Mock Browser");
            errorAlert.setHeaderText("Page Not Available");
            errorAlert.setContentText("This is a mock browser that doesn't connect to the internet.\n\n" +
                                    "Try one of these mock pages instead:\n" +
                                    "• home\n" +
                                    "• javadiscussion.com\n" +
                                    "• 1331motd.com\n" +
                                    "• aboutbrowser");
            errorAlert.showAndWait();
        }
    }

    // Save posts when the application closes
    // - called when app is supposed to close
    // - ensures data is saved
    @Override
    public void stop() {
        if (postArea != null) { // Ensure postArea is not null
            ArrayList<Post> posts = new ArrayList<>();
            for (Node node : postArea.getChildren()) { // Iterate through all posts
                if (node instanceof VBox) { // Check if node is a VBox (post container)
                    VBox postBox = (VBox) node;

                    // Skip if this is the "Be the first to make a post!" message
                    if (postBox.getChildren().size() == 1
                        && postBox.getChildren().get(0) instanceof VBox) {
                        continue;
                    }

                    // Only process if we have at least 3 children (header, body, likes)
                    if (postBox.getChildren().size() >= 3) {
                        try {
                            Text header = (Text) postBox.getChildren().get(0); // Get header
                            Text body = (Text) postBox.getChildren().get(1);   // Get body
                            Text likesText = (Text) postBox.getChildren().get(2); // Get likes

                            String headerText = header.getText();
                            String bodyText = body.getText();

                            // Parse author and post number
                            String[] parts = headerText.split(" #");
                            if (parts.length == 2) {
                                String author = parts[0];
                                int number = Integer.parseInt(parts[1]);

                                // Extract likes count
                                int likes = 0;
                                if (likesText.getText().startsWith("Likes: ")) {
                                    likes = Integer.parseInt(likesText.getText().substring(7));
                                }

                                posts.add(new Post(author, number, bodyText, likes));
                            }
                        } catch (Exception e) {
                            // If any parsing errors occur for this post, log and skip it
                            System.err.println("Error processing a post: " + e.getMessage());
                        }
                    }
                }
            }

            if (!posts.isEmpty()) {
                savePosts(posts);
            }
        }
    }

    // Loads the posts from post_history.txt and display them
    private void loadPosts(VBox postsArea) {
        postsArea.getChildren().clear(); // to clear it
        ArrayList<Post> posts = new ArrayList<>();

        File fname = new File("post_history.txt");
        if (fname.exists()) {
            try (Scanner scanner = new Scanner(fname)) {
                scanner.useDelimiter("\u001C");

                while (scanner.hasNext()) {
                    String line = scanner.next();
                    if (!line.trim().isEmpty()) { // Skip empty entries
                        Post post = Post.fromString(line);
                        posts.add(post);
                    }
                }
            } catch (FileNotFoundException e) {
                System.err.println("Error reading post_history.txt: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error processing posts: " + e.getMessage());
            }
        }
        
        // Sort posts based on current sort preference
        if (sortNewestFirst) {
            posts.sort((p1, p2) -> Integer.compare(p2.getPostNum(), p1.getPostNum())); // Newest first
        } else {
            posts.sort((p1, p2) -> Integer.compare(p1.getPostNum(), p2.getPostNum())); // Oldest first
        }

        // Post display
        // if posts thing is empty then
        if (posts.isEmpty()) {
            Text noPostsTxt = new Text("Be the first to make a post!");
            noPostsTxt.setStyle("-fx-font-size: 12px;");

            VBox noPostsContainer = new VBox(noPostsTxt);
            noPostsContainer.setAlignment(Pos.CENTER);
            postsArea.getChildren().add(noPostsContainer);
        } else {
            // Iterates over the posts, and post them one-by-one.
            for (Post post : posts) {
                VBox postBox = new VBox(5);
                postBox.setStyle("-fx-background-color: lightgray; -fx-padding: 10px;");

                Text headerText = new Text(post.getAuth() + " #" + post.getPostNum());
                headerText.setStyle("-fx-font-size: 16px;");

                Text bodyText = new Text(post.getTxtBody());
                bodyText.setStyle("-fx-font-size: 12px;");

                // Like counter display
                Text likesText = new Text("Likes: " + post.getLikes());

                // Action buttons
                Button likeButton = new Button("Like");
                Button deleteButton = new Button("Delete");

                HBox actionButtons = new HBox(10, likeButton, deleteButton);

                // Set up like button action
                likeButton.setOnAction(e -> {
                    post.incrementLikes();
                    likesText.setText("Likes: " + post.getLikes());
                    savePosts(posts); // Save updated likes to file
                });

                // Set up delete button action
                deleteButton.setOnAction(e -> {
                    posts.remove(post);
                    loadPosts(postsArea); // Refresh the post display
                    savePosts(posts); // Save updated posts to file
                });

                postBox.getChildren().addAll(headerText, bodyText, likesText, actionButtons);
                postsArea.getChildren().add(postBox);
            }
        }
    }

    // Save posts to file
    private void savePosts(ArrayList<Post> posts) {
        try (PrintWriter writer = new PrintWriter("post_history.txt")) {
            for (Post post : posts) {
                writer.print(post.toString() + "\u001C");
            }
        } catch (IOException e) {
            Alert errorAlert = new Alert(AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setContentText("Error saving posts: " + e.getMessage());
            errorAlert.showAndWait();
            System.err.println("Error occurred in saving posts: " + e.getMessage());
        }
    }

    // Pop-up window for the post-making
    private void showNewPostWindow(VBox postsArea) {
        Stage newPostStage = new Stage();
        newPostStage.initModality(Modality.APPLICATION_MODAL); // Block other windows until closed
        newPostStage.setTitle("New Post");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Text authTitle = new Text("Author:");
        authTitle.setStyle("-fx-font-size: 14px;");
        TextField authTxtField = new TextField();
        authTxtField.setPromptText("Enter your name...");
        layout.getChildren().addAll(authTitle, authTxtField);

        Text txtbodyTitle = new Text("Post Body:");
        txtbodyTitle.setStyle("-fx-font-size: 14px;");
        TextArea txtBodyField = new TextArea();
        txtBodyField.setPromptText("Type your post...");
        txtBodyField.setPrefRowCount(5);
        layout.getChildren().addAll(txtbodyTitle, txtBodyField);

        Button postButton = new Button("Post");
        layout.getChildren().add(postButton);

        Scene scene = new Scene(layout, 300, 200); // 300 x 200 poput window size
        newPostStage.setScene(scene);

        // ***************************** //
        // Lambda Expression //
        // ***************************** //
        postButton.setOnAction(event -> {
            String author = authTxtField.getText().trim();
            String body = txtBodyField.getText().trim();

            if (body.isEmpty()) {
                Alert errorAlert = new Alert(AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setContentText("Post body cannot be empty!");
                errorAlert.showAndWait();
            } else {
                if (author.isEmpty()) {
                    author = "Anonymous";
                }
                addPost(author, body, postsArea);
                newPostStage.close();
            }
        });

        newPostStage.showAndWait();
    }

    private void addPost(String author, String body, VBox postsArea) {
        ArrayList<Post> posts = new ArrayList<>();
        File outFile = new File("post_history.txt");
        
        if (outFile.exists()) {
            try (Scanner scanner = new Scanner(outFile)) {
                scanner.useDelimiter("\u001C");
                while (scanner.hasNext()) {
                    String line = scanner.next();
                    if (!line.trim().isEmpty()) {
                        posts.add(Post.fromString(line));
                    }
                }
            } catch (FileNotFoundException e) {
                System.err.println("Error reading posts: " + e.getMessage());
            }
        }

        int newPostNum = posts.size() + 1;
        Post newPost = new Post(author, newPostNum, body);
        posts.add(newPost);

        // Save to file
        savePosts(posts);
        loadPosts(postsArea);
    }

    // Helper method to create welcome view
    private VBox createWelcomeView(Text displayText1, Text displayText2) {
        displayText1.setText("Welcome!");
        displayText2.setText("Type a URL in the URL bar to get started");
        displayText1.setStyle("-fx-font-size: 16px;");
        displayText2.setStyle("-fx-font-size: 12px;");

        // VBox to stack them on top of each other - set to center
        VBox textContainer = new VBox(10, displayText1, displayText2);
        textContainer.setAlignment(Pos.CENTER);
        return textContainer;
    }
    
    // Helper method to update the window title based on current page
    private void updateTitle(String pageName) {
        primaryStage.setTitle("Mock Browser - " + pageName);
    }

    /**
     * Main method to launch the application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}