# Fict
Fitness &amp; Health Diet Tracker - Mobile Android Application developed in Java with Android Studio.

This app was developed because traditional calorie trackers suffer from the tedious task of having to manually record foods and their nutrition. Fict adds the ability to take a photo of your meal, and use Visual Recognition to classify the dish, and return nutritional information. The goal of the app was to create something useful and simple that we could see ourselves using everyday.

<img src="https://github.com/kahhauyap/Fict/blob/master/screenshots/Screenshot_20180516-163640.png" width="450" height="790">

# Home Page
The daily home screen page greets users and acts as a hub for convenient access to all the other features of the app. At the top of the page is the Calories section which displays the calorie goal, calories consumed, and calories remaining. Under it is the the Nutrition section which shows the 3 main macro nutrients: protein, fat, carbohydrates and the user's current weight which can be changed by tapping on the scale icon. Underneath that is a Food section which displays all the meals the user recorded that day. Finally on the bottom there is a task bar for easy navigation to the History, Add Food, and Macros Calculator pages.

<img src="https://github.com/kahhauyap/Fict/blob/master/screenshots/Screenshot_20180516-163729.png" width="450" height="790">

# Visual Recognition
The user has the option to either manually input nutritional information for their dish, or use the Visual Recognition option. This option allows the user to take a photo of their meal, and utilizing IBM Watson's Visual Recognition API, return a list of possible classifications for the dish.

<img src="https://github.com/kahhauyap/Fict/blob/master/screenshots/Screenshot_20180516-163900.png" width="450" height="790">

# Nutritional Information
After the user selects the correct dish, the Nutritionix API is used to return relevant caloric, and nutritional information. This information is then recorded in an SQLite database for caching, in the case of repeated food entries. This improves response time, limits accesses to the network, and reduces API calls. The information is also saved to the daily food intake table, which is then displayed on the homepage.

<img src="https://github.com/kahhauyap/Fict/blob/master/screenshots/Screenshot_20180516-163926.png" width="450" height="790">

# Macronutrient Calculator
Upon first use of the app the user is brought to a macronutrients / "If It Fits Your Macros" (IIFYM) calculator. IIFYM is a widely used dieting plan that allows you to eat any type foods you want, so long as you don't go over your caloric and macronutrient values for the day. The user fills in a few personal details to gauge their fitness needs and select a goal. The numbers are plugged into an IIFYM formula to determine the user's daily caloric and macronutrients intake. This information is then saved into the SQLite database the app will display as goals everyday.

Source: https://healthyeater.com/flexible-dieting-calculator

<img src="https://github.com/kahhauyap/Fict/blob/master/screenshots/Screenshot_20180516-163756.png" width="450" height="790">

# History
The data for the user's food intake is saved locally on a SQLite database for future access in order to track the user's progression. After selecting an entry, a snapshot of that day's daily home page is brought up so the user can view changes day by day. 

<img src="https://github.com/kahhauyap/Fict/blob/master/screenshots/Screenshot_20180516-163947.png" width="450" height="790">
<img src="https://github.com/kahhauyap/Fict/blob/master/screenshots/Screenshot_20180516-164014.png" width="450" height="790">
