CREATE TABLE author (
                        id INT AUTO_INCREMENT NOT NULL,
                        name VARCHAR(128),
                        email VARCHAR(128),
                        PRIMARY KEY(id)
);

CREATE TABLE vcf_file_descriptor (
                                   id INT AUTO_INCREMENT NOT NULL,
                                   id_author INT NOT NULL,
                                   byte_file_size BIGINT,
                                   name VARCHAR(128),
                                   file_path VARCHAR(512),
                                   index_file_path VARCHAR(512),
                                   PRIMARY KEY (id),
                                   FOREIGN KEY (id_author)
                                       REFERENCES author (id)
);
CREATE TABLE vcf_file_info_header (
                                   id INT AUTO_INCREMENT NOT NULL,
                                   id_vcf_file_descriptor INT NOT NULL,
                                   id_info  VARCHAR(16),
                                   number VARCHAR(10),
                                   type VARCHAR(32),
                                   description VARCHAR(1024),
                                   source VARCHAR(128),
                                   version VARCHAR(128),
                                   PRIMARY KEY (id),
                                   FOREIGN KEY (id_vcf_file_descriptor)
                                       REFERENCES vcf_file_descriptor (id)
                                       ON DELETE CASCADE
                                       ON UPDATE CASCADE
);
