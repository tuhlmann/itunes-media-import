itunes-media-import
===================

A small Clojure app to synchronize a folder structure
of media files with iTunes.

What is it used for?
--------------------

I use to store my media files
(recorded movies, tv serials and iTunes purchases)
on a NAS drive.
I have a little Mac Mini server that, among other things,
runs iTunes and serves these media files to connected
Apple TV's around the house. Although there are surely other
ways to do that this setup has served me well and most of all,
is easy to handle by all the Non-IT specialists in the house
and has proven a robust solution.

When I record something (EyeTV, also on the Mac Mini),
I convert it into .m4v files and save it into a specific file
structure on the NAS.

There are 3 supported media types: itunes, movie and serie.

For each supported type there exists one or more entry
paths into the file structure:

### Movies

For movies, this path might by ""/Volumes/Media/Movies".
Below the "Movies" directory there is another one,
like "Action", "Drama", etc. This one will become the genre
of the movie once its imported into iTunes.

As an example I would save the movie file "2 fast 2 furious.m4v"
into "/Volumes/Media/Movies/Action/2 fast 2 furious.m4v"

### TV Series

The folder structure for TV serials contains a bit
more information.

As an example the, the second episode of the
first season of the serial "Father Brown" is saved in the directory:

    /Volumes/Media/TV/Father Brown/S1/Father Brown 2 - Die fliegenden Sterne.m4v

It is my responsibility to save the file according to this structure.
After the root directove (TV) there is the name of the show,
the next folder is "S" followed by the season number,
this folder contains the files "[name of show] [episode no] - [episode name]".

"itunes-media-import" will parse the folder structure and add
these files to the iTunes process on the current machine.
It will keep a database of already added files so it will
only add new files in a subsequent run.

If you remove a file from your folder structure,
the tool will remove this file from iTunes and from the database
to keep folders and iTunes in sync.

### iTunes purchases

iTunes purchases just remain where iTunes puts them.
I then point the file path to the "TV Shows" folder of
my iTunes media directory.

The configuration file
----------------------

Configuration is expected in a file "config.edn"
in the main directory. A sample configuration
looks like this:

    {
      :run-mode :test
      :directories [
        {:dir "/path/to/your/iTunes/files"
         :dir-id "itunes1", :kind "itunes" }
        {:dir "/path/to/Movies"
         :dir-id "movies1", :kind "movie" }
        {:dir "/path/to/tv series"
         :dir-id "series1", :kind "serie" }
      ]  
    }

Please note: the `:run-mode` flag is currently not used.

You can include as many directories as you need
to traverse, and make sure you follow these rules:

* The dir-id needs to be a unique identifier inside you
configurations. You cannot have two directories with ID
"movies1".
* There are only these 3 recognized values for media kind,
you need to add one of these to your directory entry.
    * itunes
    * movie
    * serie


### Folder Structure

For each media type (iTunes, movies, tv shows) there should be a root folder 
somewhere on some accessible media. The structure for iTunes is given by iTunes
itself and not touched.

The movie folder tree should name a category and below that category list the
movies you wish to appear in that category.

For tv shows you should list the name of the show followed by a folder names
`S<no of season>` (for instance `tv_shows/Doctor Who/S1` for the first season
of Doctor Who).

Below that folder you copy all media files belonging to that season. That way
they will automatically be assigned the correct season when parsing the folder tree.

### Media File Naming pattern

There's a limitation in how you can name your tv show files. 
This is the pattern:


```
<name of the show> <no of episode> - <title>.m4v

```

The title should not contain a dash, as that would currently break the parser.

Have fun!
