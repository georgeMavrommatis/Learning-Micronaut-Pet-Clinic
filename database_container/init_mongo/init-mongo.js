//
// init-mongo.js
// Docker entrypoint init script for MongoDB
//

// 1) Initiate a single-node replica set so majority reads work
print("==> Initiating replica set rs0...");
rs.initiate({
  _id: "rs0",
  members: [
    { _id: 0, host: "localhost:27017" }
  ]
});

// 2) Wait for the primary to be elected
print("==> Waiting for primary...");
let isPrimary = false;
while (!isPrimary) {
  const status = rs.status();
  if (status.myState === 1) {
    isPrimary = true;
    break;
  }
  sleep(1000);
}
print("==> Primary is up.");

// 3) Create application user for the petclinic database
print("==> Creating user for petclinic database...");
const adminDB = db.getSiblingDB("admin");
adminDB.createUser({
  user: "petclinic",
  pwd:  "petclinic",
  roles: [
    { role: "readWrite", db: "petclinic" }
  ]
});
print("==> User ‘petclinic’ created.");

// 4) (Optional) Pre-create your collection
print("==> Creating vetReviews collection...");
const petDB = db.getSiblingDB("petclinic");
petDB.createCollection("vetReviews");
print("==> Initialization complete.");
