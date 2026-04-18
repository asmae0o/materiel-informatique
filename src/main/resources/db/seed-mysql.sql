INSERT INTO marque (nom)
SELECT 'Apple'
WHERE NOT EXISTS (SELECT 1 FROM marque WHERE nom = 'Apple');

INSERT INTO marque (nom)
SELECT 'Logitech'
WHERE NOT EXISTS (SELECT 1 FROM marque WHERE nom = 'Logitech');

INSERT INTO marque (nom)
SELECT 'Asus'
WHERE NOT EXISTS (SELECT 1 FROM marque WHERE nom = 'Asus');

INSERT INTO marque (nom)
SELECT 'Dell'
WHERE NOT EXISTS (SELECT 1 FROM marque WHERE nom = 'Dell');

INSERT INTO marque (nom)
SELECT 'NVIDIA'
WHERE NOT EXISTS (SELECT 1 FROM marque WHERE nom = 'NVIDIA');

INSERT INTO categorie (nom)
SELECT 'PC Portables'
WHERE NOT EXISTS (SELECT 1 FROM categorie WHERE nom = 'PC Portables');

INSERT INTO categorie (nom)
SELECT 'PC de Bureau'
WHERE NOT EXISTS (SELECT 1 FROM categorie WHERE nom = 'PC de Bureau');

INSERT INTO categorie (nom)
SELECT 'Peripheriques'
WHERE NOT EXISTS (SELECT 1 FROM categorie WHERE nom = 'Peripheriques');

INSERT INTO categorie (nom)
SELECT 'Composants PC'
WHERE NOT EXISTS (SELECT 1 FROM categorie WHERE nom = 'Composants PC');

INSERT INTO produit (nom, description, prix, quantite_stock, categorie_id, marque_id)
SELECT 'MacBook Pro 14" M3', 'Puce Apple M3, 16 Go RAM, 512 Go SSD. Le summum de la performance pour les createurs.', 22500.0, 15,
       (SELECT id FROM categorie WHERE nom = 'PC Portables' LIMIT 1),
       (SELECT id FROM marque WHERE nom = 'Apple' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM produit WHERE nom = 'MacBook Pro 14" M3');

INSERT INTO produit (nom, description, prix, quantite_stock, categorie_id, marque_id)
SELECT 'Souris Ergonomique MX Master 3S', 'Souris sans fil ultra-precise avec clics silencieux et molette MagSpeed.', 1200.0, 50,
       (SELECT id FROM categorie WHERE nom = 'Peripheriques' LIMIT 1),
       (SELECT id FROM marque WHERE nom = 'Logitech' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM produit WHERE nom = 'Souris Ergonomique MX Master 3S');

INSERT INTO produit (nom, description, prix, quantite_stock, categorie_id, marque_id)
SELECT 'Carte Graphique GeForce RTX 4070', '12 Go GDDR6X, architecture Ada Lovelace. Ideale pour le gaming en 1440p et la 3D.', 8500.0, 8,
       (SELECT id FROM categorie WHERE nom = 'Composants PC' LIMIT 1),
       (SELECT id FROM marque WHERE nom = 'NVIDIA' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM produit WHERE nom = 'Carte Graphique GeForce RTX 4070');

INSERT INTO produit (nom, description, prix, quantite_stock, categorie_id, marque_id)
SELECT 'Ecran Dell UltraSharp 27" 4K', 'Moniteur 4K USB-C, couleurs parfaites pour les designers et developpeurs.', 6500.0, 20,
       (SELECT id FROM categorie WHERE nom = 'Peripheriques' LIMIT 1),
       (SELECT id FROM marque WHERE nom = 'Dell' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM produit WHERE nom = 'Ecran Dell UltraSharp 27" 4K');

INSERT INTO produit (nom, description, prix, quantite_stock, categorie_id, marque_id)
SELECT 'PC Gamer Asus ROG Strix', 'Intel Core i9, 32 Go RAM, 1 To NVMe, refroidissement liquide.', 28000.0, 5,
       (SELECT id FROM categorie WHERE nom = 'PC de Bureau' LIMIT 1),
       (SELECT id FROM marque WHERE nom = 'Asus' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM produit WHERE nom = 'PC Gamer Asus ROG Strix');
