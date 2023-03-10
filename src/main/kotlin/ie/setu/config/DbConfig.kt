package ie.setu.config

import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.name

class DbConfig{
    private val logger = KotlinLogging.logger {}

    //NOTE: you need the ?sslmode=require otherwise you get an error complaining about the ssl certificate
    fun getDbConnection() :Database{

        /*logger.info{"Starting DB Connection..."}

        val dbConfig = Database.connect(
            "jdbc:postgresql://ec2-107-23-76-12.compute-1.amazonaws.com:5432/d2d24n666u9ral?sslmode=require",
            driver = "org.postgresql.Driver",
            user = "ajvhwjdgyhzgpf",
            password = "5057321d6ce19b82a9dc35713c7d8c438e84cc2f0920a10aacf7769b6df2c23f")

        logger.info{"DbConfig name = " + dbConfig.name}
        logger.info{"DbConfig url = " + dbConfig.url}

        return dbConfig*/

        val logger = KotlinLogging.logger {}
        logger.info{"Starting DB Connection..."}

        val PGUSER = "uqqukgcr"
        val PGPASSWORD = "IZW40mblvpZKp4Z3QZ_Fd1wICJIvqmDE"
        val PGHOST = "lucky.db.elephantsql.com"
        val PGPORT = "5432"
        val PGDATABASE = "uqqukgcr"

        //url format should be jdbc:postgresql://host:port/database
        val url = "jdbc:postgresql://$PGHOST:$PGPORT/$PGDATABASE"

        val dbConfig = Database.connect(url,
            driver="org.postgresql.Driver",
            user = PGUSER,
            password = PGPASSWORD
        )

        logger.info{"db url - connection: " + dbConfig.url}

        return dbConfig
    }
}














