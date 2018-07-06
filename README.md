# Fict
Fitness &amp; Health Diet Tracker - Mobile Android Application developed in Java with Android Studio.


# Home Page
<img src="https://github.com/kahhauyap/Fict/blob/master/screenshots/Screenshot_20180516-163729.png" width="450" height="790">
The daily home screen page greets users and acts as a hub for convenient access to all the other features of the app. A daily summary of caloric and nutritional goals, as well as foods eaten are displayed.  

# Visual Recognition
https://github.com/kahhauyap/Fict/blob/master/screenshots/Screenshot_20180516-163900.png

The user has the option to either manually input nutritional information for their dish, or use the Visual Recognition option. This allows the user to take a photo of food, and utilizing IBM Watson's Visual Recognition API return a list of possible classifications for the dish.

# Nutritional Information
https://github.com/kahhauyap/Fict/blob/master/screenshots/Screenshot_20180516-163926.png

After the user selects the correct dish, the Nutritionix API is used to return relevant caloric, and nutritional information. This information is then recorded in an SQLite database for caching for future query in order to avoid repeated calls to the API. The information is also saved to the daily food intake, updating the values on the Home page.

# History

https://github.com/kahhauyap/Fict/blob/master/screenshots/Screenshot_20180516-163947.png
https://github.com/kahhauyap/Fict/blob/master/screenshots/Screenshot_20180516-164014.png

The data for the user's food intake is saved locally on a SQLite database for future access in order to track the user's progression. After selecting an entry, a snapshot of that day's daily home page is brought up so the user can view changes day by day.
