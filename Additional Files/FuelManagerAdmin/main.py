import firebase_admin
from firebase_admin import db
from firebase_admin import auth
from firebase_admin import credentials


def show_users():
    ref = db.reference('users')
    users = ref.get()
    for x in users:
        print("\n", "User ID: ", x)
        for y in users[x]:
            print("\t", y, ':', users[x][y])


def add_user():
    email = input("Enter Email: ")
    password = input("Enter Password: ")
    try:
        user = auth.create_user(
            email=email,
            email_verified=True,
            password=password
        )

        city = input("Enter city: ")
        address = input("Enter address: ")
        firstName = input("Enter firstName: ")
        lastName = input("Enter lastName: ")

        users = db.reference('users')
        users.child(user.uid).set({'address': address, 'budget': 0, 'city': city, 'firstName': firstName, 'lastName': lastName})
        print("User: ", user.uid, " successfully created")
    except ValueError:
        print("Invalid arguments")


def delete_user():
    uid = input("Enter User ID: ")
    try:
        auth.delete_user(uid=uid)
        users = db.reference('users')
        users.child(uid).delete()
        print("Successfully deleted user")
    except firebase_admin.auth.AuthError:
        print("User not found")


cred = credentials.Certificate("fuelmanager-188122-firebase-adminsdk-ssjoq-d64a420c68.json")

firebase_admin.initialize_app(cred, {'databaseURL': 'https://fuelmanager-188122.firebaseio.com/'})

running = True
while running:
    print("""
1.Show Users Database
2.Add a User
3.Delete a User
4.Exit/Quit
""")

    ans = input("What would you like to do? ")
    if ans == "1":
        show_users()
    elif ans == "2":
        add_user()
    elif ans == "3":
        delete_user()
    elif ans == "4":
        running = False
    else:
        print("\n Not Valid Choice Try again")


