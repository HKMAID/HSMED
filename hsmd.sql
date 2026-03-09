-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : lun. 09 mars 2026 à 11:42
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `hsmd`
--

-- --------------------------------------------------------

--
-- Structure de la table `creneau`
--

CREATE TABLE `creneau` (
  `matCren` int(6) NOT NULL,
  `debut` datetime NOT NULL,
  `fin` datetime NOT NULL,
  `etatCren` enum('disponible','indisponible','réservé') NOT NULL DEFAULT 'disponible'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `creneau`
--

INSERT INTO `creneau` (`matCren`, `debut`, `fin`, `etatCren`) VALUES
(1, '2025-11-25 08:00:00', '2025-11-25 09:00:00', 'indisponible'),
(2, '2025-11-25 09:00:00', '2025-11-25 10:00:00', 'indisponible'),
(3, '2025-11-25 10:00:00', '2025-11-25 11:00:00', 'indisponible'),
(4, '2025-11-25 11:00:00', '2025-11-25 12:00:00', 'indisponible'),
(5, '2025-11-25 12:00:00', '2025-11-25 13:00:00', 'indisponible'),
(6, '2025-11-25 13:00:00', '2025-11-25 14:00:00', 'indisponible'),
(7, '2025-11-25 14:00:00', '2025-11-25 15:00:00', 'indisponible'),
(8, '2025-11-25 15:00:00', '2025-11-25 16:00:00', 'indisponible'),
(9, '2025-11-27 08:00:00', '2025-11-27 09:00:00', 'indisponible'),
(10, '2025-11-27 09:00:00', '2025-11-27 10:00:00', 'indisponible'),
(11, '2025-12-14 14:00:00', '2025-12-14 15:00:00', 'disponible'),
(13, '2025-12-10 12:00:00', '2025-12-10 13:00:00', 'indisponible'),
(14, '2025-11-29 14:00:00', '2025-11-29 15:00:00', 'indisponible'),
(15, '2025-11-27 10:00:00', '2025-11-27 11:00:00', 'indisponible'),
(16, '2025-11-27 11:00:00', '2025-11-27 12:00:00', 'indisponible'),
(17, '2025-11-27 12:00:00', '2025-11-27 13:00:00', 'indisponible'),
(18, '2025-11-27 13:00:00', '2025-11-27 14:00:00', 'indisponible'),
(19, '2025-11-27 14:00:00', '2025-11-27 15:00:00', 'indisponible'),
(20, '2025-11-27 15:00:00', '2025-11-27 16:00:00', 'indisponible'),
(21, '2025-12-11 08:00:00', '2025-12-11 09:00:00', 'réservé'),
(22, '2025-12-11 09:00:00', '2025-12-11 10:00:00', 'réservé'),
(23, '2025-12-11 10:00:00', '2025-12-11 11:00:00', 'disponible'),
(24, '2025-12-11 11:00:00', '2025-12-11 12:00:00', 'disponible'),
(25, '2025-12-11 13:00:00', '2025-12-11 14:00:00', 'réservé'),
(26, '2025-12-11 14:00:00', '2025-12-11 15:00:00', 'disponible'),
(27, '2025-12-11 15:00:00', '2025-12-11 16:00:00', 'disponible'),
(28, '2025-12-22 10:00:00', '2025-12-22 11:00:00', 'disponible');

-- --------------------------------------------------------

--
-- Structure de la table `dossiermedical`
--

CREATE TABLE `dossiermedical` (
  `matDoss` int(5) NOT NULL,
  `matPat` int(5) NOT NULL,
  `dateCreation` date NOT NULL,
  `grpSanguin` enum('A+','A-','B+','B-','AB+','AB-','O+','O-') NOT NULL,
  `remarques` text NOT NULL,
  `traitements` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `dossiermedical`
--

INSERT INTO `dossiermedical` (`matDoss`, `matPat`, `dateCreation`, `grpSanguin`, `remarques`, `traitements`) VALUES
(1, 30001, '2025-11-18', 'A+', 'Blessure LC', '- Opération en urgence\r\n- Réeducation assistée sur 6 mois\r\n\r\n'),
(2, 30004, '2025-11-19', 'B-', 'Myopie', 'Séances LASIK'),
(3, 30002, '2025-11-20', 'AB+', 'WIWI', 'MIMICHE'),
(4, 30003, '2025-12-11', 'O-', 'Forte fièvre ', 'Panadol');

-- --------------------------------------------------------

--
-- Structure de la table `medecin`
--

CREATE TABLE `medecin` (
  `matMed` int(5) NOT NULL,
  `specialite` varchar(25) NOT NULL,
  `adresseCabinet` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `medecin`
--

INSERT INTO `medecin` (`matMed`, `specialite`, `adresseCabinet`) VALUES
(10001, 'Cardiologue', '20 Rue du congrès - Ouzellaguen');

-- --------------------------------------------------------

--
-- Structure de la table `patient`
--

CREATE TABLE `patient` (
  `matPat` int(5) NOT NULL,
  `nomPat` varchar(50) NOT NULL,
  `prenomPat` varchar(50) NOT NULL,
  `sexe` enum('M','F') NOT NULL,
  `dateNaiss` date NOT NULL,
  `numTelephone` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `patient`
--

INSERT INTO `patient` (`matPat`, `nomPat`, `prenomPat`, `sexe`, `dateNaiss`, `numTelephone`) VALUES
(30001, 'Boulbina', 'Adel', 'M', '2002-04-15', '+213794937245'),
(30002, 'Salmi', 'Nadia', 'F', '1997-10-23', '+213624319604'),
(30003, 'Idri', 'Nasser', 'M', '2005-05-17', '+213568718213'),
(30004, 'Berraki', 'Lyes', 'M', '1999-10-05', '+213540945682'),
(30005, 'Allouache', 'Wassila', 'F', '2003-11-20', '+213511223369');

-- --------------------------------------------------------

--
-- Structure de la table `rdv`
--

CREATE TABLE `rdv` (
  `matCren` int(6) NOT NULL,
  `matPat` int(5) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `rdv`
--

INSERT INTO `rdv` (`matCren`, `matPat`) VALUES
(13, 30001),
(14, 30002),
(9, 30003),
(1, 30004),
(21, 30004),
(22, 30004),
(10, 30005),
(25, 30005);

-- --------------------------------------------------------

--
-- Structure de la table `secretaire`
--

CREATE TABLE `secretaire` (
  `matSec` int(5) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `secretaire`
--

INSERT INTO `secretaire` (`matSec`) VALUES
(20001);

-- --------------------------------------------------------

--
-- Structure de la table `utilisateur`
--

CREATE TABLE `utilisateur` (
  `matUtili` int(5) NOT NULL,
  `nomUtili` varchar(50) NOT NULL,
  `prenomUtili` varchar(50) NOT NULL,
  `mdpUtili` varchar(120) NOT NULL,
  `derniereCnx` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `utilisateur`
--

INSERT INTO `utilisateur` (`matUtili`, `nomUtili`, `prenomUtili`, `mdpUtili`, `derniereCnx`) VALUES
(10001, 'Kaci', 'Mohand Ameziane', 'MpDremxVi9L4YAxe9w+zvkz8j+0dWHqZ1Oa4RdGd4udvEog3wIwL93/Cm6dIgONA', '2025-12-11 08:39:48'),
(20001, 'Benfares', 'Lyna', 'CyjoOJDIJHG4i4sT7WMNKBSn1XNSDHILUCipM4w51RIqCVQZTExJze2RnB7ImTyH', '2025-12-11 08:42:17');

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `creneau`
--
ALTER TABLE `creneau`
  ADD PRIMARY KEY (`matCren`);

--
-- Index pour la table `dossiermedical`
--
ALTER TABLE `dossiermedical`
  ADD PRIMARY KEY (`matDoss`,`matPat`),
  ADD KEY `matPat` (`matPat`);

--
-- Index pour la table `medecin`
--
ALTER TABLE `medecin`
  ADD PRIMARY KEY (`matMed`),
  ADD UNIQUE KEY `libelleSalle` (`adresseCabinet`);

--
-- Index pour la table `patient`
--
ALTER TABLE `patient`
  ADD PRIMARY KEY (`matPat`),
  ADD UNIQUE KEY `numTelephone` (`numTelephone`),
  ADD KEY `i1` (`nomPat`,`prenomPat`) USING HASH;

--
-- Index pour la table `rdv`
--
ALTER TABLE `rdv`
  ADD PRIMARY KEY (`matCren`),
  ADD KEY `matPat` (`matPat`);

--
-- Index pour la table `secretaire`
--
ALTER TABLE `secretaire`
  ADD PRIMARY KEY (`matSec`);

--
-- Index pour la table `utilisateur`
--
ALTER TABLE `utilisateur`
  ADD PRIMARY KEY (`matUtili`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `creneau`
--
ALTER TABLE `creneau`
  MODIFY `matCren` int(6) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- AUTO_INCREMENT pour la table `dossiermedical`
--
ALTER TABLE `dossiermedical`
  MODIFY `matDoss` int(5) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT pour la table `patient`
--
ALTER TABLE `patient`
  MODIFY `matPat` int(5) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30006;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `dossiermedical`
--
ALTER TABLE `dossiermedical`
  ADD CONSTRAINT `dossiermedical_ibfk_1` FOREIGN KEY (`matPat`) REFERENCES `patient` (`matPat`);

--
-- Contraintes pour la table `medecin`
--
ALTER TABLE `medecin`
  ADD CONSTRAINT `medecin_ibfk_1` FOREIGN KEY (`matMed`) REFERENCES `utilisateur` (`matUtili`);

--
-- Contraintes pour la table `rdv`
--
ALTER TABLE `rdv`
  ADD CONSTRAINT `rdv_ibfk_1` FOREIGN KEY (`matCren`) REFERENCES `creneau` (`matCren`),
  ADD CONSTRAINT `rdv_ibfk_2` FOREIGN KEY (`matPat`) REFERENCES `patient` (`matPat`);

--
-- Contraintes pour la table `secretaire`
--
ALTER TABLE `secretaire`
  ADD CONSTRAINT `secretaire_ibfk_1` FOREIGN KEY (`matSec`) REFERENCES `utilisateur` (`matUtili`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
