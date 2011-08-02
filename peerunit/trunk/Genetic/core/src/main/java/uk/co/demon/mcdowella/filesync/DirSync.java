package uk.co.demon.mcdowella.filesync;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Date;
import java.text.DateFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.io.PrintWriter;

/** This class contains the methods for working with version histories
 *  held in a directory tree. Each directory in the original is
 *  represented by a directory in the version tree, whose name is
 *  the original name prefixed by DIR_PREFIX. Each file in the original
 *  is represented by a directory in the version tree, whose name is
 *  prefixed by FILE_PREFIX. Both kinds of directories contain a file
 *  listing their version histories, showing for instance whether the
 *  object exists at given versions. Directories representing files
 *  contain a copy of the most recent version, plus difference between
 *  adjacent versions. A separate version file, in the topmost
 *  directory, keeps track of the versions and their associated dates.
 */
public class DirSync
{
  /** top level directory of backup/resync area */
  private File topLevel;
  /** file in top level directory containing version info */
  private File versionFile;

  /** name of version file */
  private static final String TOP_VERSION_FILE = "versions";
  /** header for version file */
  private static final String TOP_VERSION_HEADER= "DirSyncTop20021214";
  /** name of alternate version file. Note that none of the files
   *  given here start with either FILE_PREFIX or DIR_PREFIX.
   */
  private static final String ALTERNATE_TOP_VERSION_FILE =
    "pending_versions";
  /** string after version number in version-timestamp line of version 
   *  file. Also used to terminate version number in file and directory
   *  version files. In this case it is followed by E (exists) or
   *  X (deleted at that version number)
   */
  private static final String AFTER_VERSION_NUMBER = ":";
  /** code for exists in version file */
  private static final String VERSION_EXISTS = "E";
  /** code for deleted in version file */
  private static final String VERSION_DELETED = "X";
  /** Files are stored using directories. This prefix is given to all
   *  names of directories used to store files
   */
  private static final String FILE_PREFIX = "f";
  /** Directories are represented using other directories, with the
   *  following prefix:
   */
  private static final String DIR_PREFIX = "d";
  /** Header for version file of directory and file version info */
  private static final String DATA_VERSION_HEADER =
    "DirSyncData20021204";
  /** Name of current data file. Difference files have a version
   *  number appended to this: the number of the version this
   *  difference file can be used to create.
   */
  private static final String CURRENT_DATA_FILE = "current_data";
  /** name of alternate current data file. Difference files have a
   *  version number appended to this, but this is the number of
   *  the newest version, which isn't really that helpful.
   */
  private static final String ALTERNATE_DATA_FILE = "pending_data";
  /** Name of file containing version of current data file.
   */
  private static final String CURRENT_VERSION_FILE =
    "current_version";
  /** Name of file containing alternate version of current data file. */
  private static final String ALTERNATE_VERSION_FILE =
    "pending_version";
  /** skip factor in file comparison algorithm */
  private static final double SKIP_FACTOR = 1.5;
  /** maximum insert length in file comparison algorithm */
  private static final int MAX_INSERT_LEN = 10000;
  /** first version number to allocate */
  private static final int FIRST_VERSION_NUMBER = 1;

  private static String[] ALTERNATE_PREFIXES = new String[]
  {
    ALTERNATE_TOP_VERSION_FILE,
    ALTERNATE_DATA_FILE,
    ALTERNATE_VERSION_FILE
  };

  /** Create a DirSync object looking at a directory containing an
   *  existing version file. If none found in the target directory
   *  and lookUp is set, try parent directories.
   */
  public DirSync(File f, boolean lookUp) throws DirSyncUserException
  {
    topLevel = f;
    for (;;)
    {
      if (!topLevel.exists())
      {
	throw new DirSyncUserException(
	  "Directory " + topLevel + " does not exist");
      }
      versionFile = new File(topLevel, TOP_VERSION_FILE);
      if (versionFile.exists())
      {
        break;
      }
      // System.err.println("Could not find " + versionFile);
      if (!lookUp)
      {
        throw new DirSyncUserException(
	  "Could not find version file in top level directory");
      }
      topLevel = topLevel.getParentFile();
      if (topLevel == null)
      {
        throw new DirSyncUserException(
	  "Could not find version file in directory or any of its parents");
      }
    }
  }

  /** remove alternate files from a directory. Return list of
   *  files as of before the removal
   */
  private static File[] removeAlternates(File dir, boolean verbose) 
    throws DirSyncInternalException
  {
    File[] children = dir.listFiles();
    for (int i = 0; i < children.length; i++)
    {
      File here = children[i];
      String nameHere = here.getName();
      // Only a small number of file prefixes so go through them
      // sequentially
      for (int j = 0; j < ALTERNATE_PREFIXES.length; j++)
      {
        if (nameHere.startsWith(ALTERNATE_PREFIXES[j]))
	{
	  if (verbose)
	  {
	    System.err.println("Remove alternate " + here.getPath());
	  }
	  if (!here.delete())
	  {
	    throw new DirSyncInternalException("Could not delete " +
	      here.getPath() + " in clean");
	  }
	  break;
	}
      }
    }
    return children;
  }

  /** remove file or directory and subdirectories. Return true if
   *  no longer exists at time of return. Tries to avoid going wild on
   *  links elsewhere by only recursing to subdirectories and subfiles
   *  that have a longer canonical path than the current directory.
   */
  private static boolean removeRecursive(File d, boolean verbose)
  {
    if (!d.exists())
    {
      return true;
    }
    if (d.isFile())
    {
      if (verbose)
      {
        System.err.println("remove " + d.getPath());
      }
      return d.delete();
    }
    if (!d.isDirectory())
    {
      System.err.println("Did not try to remove funny file " +
        d.getPath());
      return false;
    }
    try
    {
      String canon = d.getCanonicalPath();
      int canonLen = canon.length();
      File[] children = d.listFiles();
      for (int i = 0; i < children.length; i++)
      {
	File here = children[i];
	if (here.getCanonicalPath().length() <= canonLen)
	{ // Assume some sort of link up or elsewhere
	  continue;
	}
	if (!removeRecursive(here, verbose))
	{
	  return false;
	}
      }
    }
    catch (IOException ioe)
    {
      System.err.println("Got exception " + ioe +
        " trying to get canonical path near " + d.getPath());
      return false;
    }
    if (verbose)
    {
      System.err.println("remove " + d.getPath());
    }
    return d.delete();
  }

  /** remove pending files and other junk left from half-completed
   *  operation
   */
  public static void clean(File dir, boolean verbose)
    throws DirSyncInternalException
  {
    String name = dir.getName();
    if (name.length() == 0)
    {
      throw new DirSyncInternalException(
        "Bad directory name len 0 at " + dir.getPath());
    }
    if (name.startsWith(FILE_PREFIX))
    { // this directory represents a file
      removeAlternates(dir, verbose);
      File versionInfo = new File(dir, CURRENT_VERSION_FILE);
      File data = new File(dir, CURRENT_DATA_FILE);
      if (!(versionInfo.exists() && data.exists()))
      { // had not yet created necessary files
	if (verbose)
	{
	  System.err.println("No files for dir " + dir.getPath());
	}
        if (!removeRecursive(dir, verbose))
	{
	  throw new DirSyncInternalException("Could not remove " +
	    dir.getPath());
	}
      }
    }
    else if (name.startsWith(DIR_PREFIX))
    { // this directory represents another directory
      File[] oldFiles = removeAlternates(dir, verbose);
      File versionInfo = new File(dir, CURRENT_VERSION_FILE);
      if (!versionInfo.exists())
      { // had not yet created necessary file
	if (verbose)
	{
	  System.err.println("No files for " + dir.getPath());
	}
        if (!removeRecursive(dir, verbose))
	{
	  throw new DirSyncInternalException("Could not remove " +
	    dir.getPath());
	}
      }
      else
      {
	try
	{
	  int lenHere = dir.getCanonicalPath().length();
	  for (int i = 0; i < oldFiles.length; i++)
	  {
	    File f = oldFiles[i];
	    if (f.exists() && f.isDirectory() && 
	      (f.getCanonicalPath().length() > lenHere))
	    {
	      clean(f, verbose);
	    }
	  }
	}
	catch (IOException ioe)
	{
	  throw new DirSyncInternalException("Got IOException " +
	    ioe + " trying to get canonical path near " +
	    dir.getPath());
	}
      }
    }
    else
    {
      throw new DirSyncInternalException("Bad directory name at " +
        dir.getPath());
    }
  }

  /** Update version file, returning last version number in file
   *  or -1. Set exists to show whether or not the file currently
   *  exists in the thing we are syncing to. Mover records moves
   *  required to finish the job.
   */
  private long addVersion(File versionFile, File alternateVersionFile,
    long currentVersion, boolean exists, FileMover mover)
    throws DirSyncInternalException
  {
    Closer closer = new Closer();
    try
    {
      BufferedReader br = null;
      if (versionFile.exists())
      {
	br = new BufferedReader(new FileReader(versionFile));
	closer.addReader(br, versionFile.getPath());
	String info = br.readLine();
	if (!DATA_VERSION_HEADER.equals(info))
	{
	  throw new DirSyncInternalException(
	    "Could not recognise version file header");
	}
      }
      PrintWriter pw = new PrintWriter(
        new BufferedWriter(new FileWriter(alternateVersionFile)));
      closer.addWriter(pw, alternateVersionFile.getPath());
      pw.println(DATA_VERSION_HEADER);
      long result = -1;
      // Used to check that version numbers are increasing. Can
      // start this off at zero, because version numbers used start
      // at 1.
      long lastVersion = FIRST_VERSION_NUMBER - 1;
      if (br != null)
      {
	for (;;)
	{
	  String info = br.readLine();
	  if (info == null)
	  {
	    break;
	  }
	  int index = info.indexOf(AFTER_VERSION_NUMBER);
	  if (index < 0)
	  {
	    throw new DirSyncInternalException(
	      "Could not find version number prefix in version file");
	  }
	  try
	  {
	    result = (new Long(info.substring(0, index).trim())).
	      longValue();
	  }
	  catch (NumberFormatException nfe)
	  {
	    throw new DirSyncInternalException(
	      "Could not read version number in version file");
	  }
	  if (result <= lastVersion)
	  {
	    throw new DirSyncInternalException(
	      "Index numbers going backwards in version file");
	  }
	  lastVersion = result;
	  pw.println(info);
	}
      }
      if (result >= currentVersion)
      {
        throw new DirSyncInternalException(
	  "Trying to add version <= lastest version present");
      }
      pw.print(Long.toString(currentVersion));
      pw.print(AFTER_VERSION_NUMBER);
      if (exists)
      {
        pw.println(VERSION_EXISTS);
      }
      else
      {
        pw.println(VERSION_DELETED);
      }
      IOException e = (IOException) closer.close();
      if (e != null)
      {
        throw e;
      }
      if (pw.checkError())
      {
        throw new DirSyncInternalException(
	  "Error handling version file");
      }
      mover.addMoveRequest(alternateVersionFile, versionFile,
        "addVersion");
      return result;
    }
    catch (IOException ie)
    {
      throw new DirSyncInternalException("Exception handling version file");
    }
    finally
    {
      IOException ie = (IOException)closer.close();
      if (ie != null)
      {
        throw new DirSyncInternalException(
	  "Exception closing version file");
      }
    }
  }

  /** 
   *  Return version and date info as held in top-level version file: 
   * version number then ":" then full date
   */
  private static String getVersionString(long version)
  {
    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG,
      DateFormat.LONG);
    String now = df.format(new Date());
    return "" + version + AFTER_VERSION_NUMBER + now;
  }

  /** Create top level directory with new version info file
   *  and a starting current version file
   */
  public static DirSync createTopLevel(File f) 
    throws DirSyncInternalException, DirSyncUserException
  {
    if (!f.getName().startsWith(DIR_PREFIX))
    {
      throw new DirSyncUserException(
        "Directory name does not start with directory prefix " +
	  DIR_PREFIX);
    }
    if (!f.mkdir())
    {
      throw new DirSyncInternalException(
        "Could not create top level directory " + f);
    }
    // Top version file, recording every version and its date
    File version = new File(f, TOP_VERSION_FILE);
    PrintWriter pw = null;
    Closer closer = new Closer();
    try
    {
      pw = new PrintWriter(new FileWriter(version));
      closer.addWriter(pw, version.getPath());
      pw.println(TOP_VERSION_HEADER);
      pw.println(getVersionString(FIRST_VERSION_NUMBER));
      if (pw.checkError())
      {
	throw new DirSyncInternalException(
	  "Error writing version file");
      }
      // File recording modifications to this directory - just
      // its creations and deletions
      File cv = new File(f, CURRENT_VERSION_FILE);
      pw = new PrintWriter(new FileWriter(cv));
      closer.addWriter(pw, cv.getPath());
      pw.println(DATA_VERSION_HEADER);
      pw.println(Integer.toString(FIRST_VERSION_NUMBER) +
        AFTER_VERSION_NUMBER + VERSION_EXISTS);
    }
    catch (IOException ioe)
    {
      throw new DirSyncInternalException(
        "Could not write version file " + version.getPath() +
	  ": " + ioe);
    }
    finally
    {
      closer.close();
    }
    return new DirSync(f, false);
  }

  /** Copy file from source to dest */
  private static void copyFile(File source, File dest) 
    throws IOException
  {
    byte[] data = new byte[10000];
    Closer closer = new Closer();
    IOException io = null;
    try
    {
      FileOutputStream fo = new FileOutputStream(dest);
      closer.addOutputStream(fo, dest.getPath());
      FileInputStream fi = new FileInputStream(source);
      closer.addInputStream(fi, source.getPath());
      for (;;)
      {
	int got = fi.read(data);
	if (got <= 0)
	{
	  break;
	}
	fo.write(data, 0, got);
      }
      return;
    }
    catch (IOException e)
    {
      io = e;
    }
    finally
    {
      IOException e = (IOException)closer.close();
      if (io == null)
      {
        io = e;
      }
      if (io != null)
      {
        throw io;
      }
    }
  }

  /** Synchronize a single file. If has just sprung into existence,
   *  copy over new file and record its existence. If no longer exists,
   *  just record this fact. Otherwise generate a difference file to
   *  produce the old from the new, move a copy of the new in as data,
   *  and shove the difference file in with the old file's version
   *  number. If our 'file' is a directory, treat it as if it no longer
   *  exists, to cater for cases when files have vanished and been
   *  replaced by directories of the same name.
   */
  private void syncFile(File syncFrom, File syncToDir, 
    long version, FileMover mover, boolean verbose)
      throws DirSyncUserException, DirSyncInternalException
  {
    if (verbose)
    {
      System.err.println("Sync file " + syncFrom.getPath() +
	" with directory " + syncToDir.getPath());
    }
    if (!syncToDir.getName().startsWith(FILE_PREFIX))
    {
      throw new DirSyncUserException("File sync directory does not " +
        "start with " + FILE_PREFIX);
    }
    if (!syncToDir.exists())
    {
      if (!syncToDir.mkdir())
      {
        throw new DirSyncInternalException(
	  "Could not create directory " + syncToDir);
      }
    }
    Closer closer = new Closer();
    IOException ioe = null;
    try
    {
      File currentData = new File(syncToDir, CURRENT_DATA_FILE);
      File alternateVersionFile = new File(syncToDir,
	ALTERNATE_VERSION_FILE);
      // Could have a file that has been replaced by a directory.
      // Treat this as if file has just vanished.
      boolean currentExists = syncFrom.exists() && syncFrom.isFile();
      File currentVersionFile =
        new File(syncToDir, CURRENT_VERSION_FILE);
      if (!currentData.exists())
      { // No previous data for this file
        if (!currentExists)
	{
	  return;
	}
        addVersion(currentVersionFile, alternateVersionFile,
	  version, currentExists, mover);
        // copy over data
	File alternateData = new File(syncToDir, ALTERNATE_DATA_FILE);
	copyFile(syncFrom, alternateData);
	mover.addMoveRequest(alternateData, currentData,
	  "syncFile data copy 401 no prev");
        return;
      }
      // current data exists
      if (!currentVersionFile.exists())
      {
        throw new DirSyncInternalException("Version file " +
	  currentVersionFile + " does not exist");
      }
      if (!currentExists)
      {
	addVersion(currentVersionFile, alternateVersionFile, version,
	  currentExists, mover);
        return;
      }
      // create difference file. Append version # of new version
      // even though that isn't right - the final file will
      // have the old version number on it, but we don't find that
      // out till we update the version number info, and we don't
      // want to do that till we have created the difference file
      // and looked to see if there is any difference.
      File differenceFile = new File(syncToDir,
        ALTERNATE_DATA_FILE + version);
      // set up and run difference algorithm to create file of
      // commands creating old file from new file
      RAFileWrapper source = new RAFileWrapper(syncFrom);
      closer.addCloseable(source, syncFrom.getPath());
      RAFileWrapper target = new RAFileWrapper(currentData);
      closer.addCloseable(target, currentData.getPath());
      FileTransformBuilder tb =
        new FileTransformBuilder(differenceFile);
      closer.addCloseable(tb, differenceFile.getPath());
      try
      {
	if (verbose)
	{
	  System.err.println("Source file is " + syncFrom +
	    " target file is " + currentData);
	}
	FileSync.createFrom(source, target, tb, SKIP_FACTOR,
	  MAX_INSERT_LEN);
      }
      catch (FileSyncException fse)
      {
	// System.err.println(fse);
	// fse.printStackTrace();
        throw new DirSyncInternalException("Error synchronising file " +
	  source + " with " + target);
      }
      IOException ee = (IOException)closer.close();
      if (ioe == null)
      {
        ioe = ee;
      }
      boolean noChange = 
        FileTransformBuilder.checkTransform(differenceFile,
        syncFrom, currentData);
      if (noChange)
      {
	// Must check that the file actually exists at the
	// previous version: if the file was deleted and has
	// been replaced exactly the same as before, we still
	// need a difference file and a version info update
	boolean existsAtPrev = false;
	try
	{
	  BufferedReader br = new BufferedReader(
	    new FileReader(currentVersionFile));
	  closer.addReader(br, currentVersionFile.getPath());
	  String existsTail = AFTER_VERSION_NUMBER + VERSION_EXISTS;
	  String deadTail = AFTER_VERSION_NUMBER + VERSION_DELETED;
	  String headerLine = br.readLine();
	  if (!DATA_VERSION_HEADER.equals(headerLine))
	  {
	    throw new DirSyncInternalException(
	      "Version header line is " + headerLine);
	  }
	  for (;;)
	  {
	    String line = br.readLine();
	    if (line == null)
	    {
	      break;
	    }
	    if (line.endsWith(existsTail))
	    {
	      existsAtPrev = true;
	    }
	    else if (line.endsWith(deadTail))
	    {
	      existsAtPrev = false;
	    }
	    else
	    {
	      throw new DirSyncInternalException(
	        "Did not recognise line '" + line +
		"' in version file " + currentVersionFile.getPath());
	    }
	  }
	}
	catch (IOException ie)
	{
	  throw new DirSyncInternalException(
	    "IO Exception checking for existence in version file", ie);
	}
	
	if (existsAtPrev)
	{
	  if (!differenceFile.delete())
	  {
	    throw new DirSyncInternalException(
	      "Could not delete unnecessary difference file " +
	        differenceFile.getPath());
	  }
	  return;
	}
      }
      long oldVersion = addVersion(currentVersionFile,
        alternateVersionFile, version, currentExists, mover);
      // copy over data
      File alternateFile =
        new File(syncToDir, ALTERNATE_DATA_FILE);
      copyFile(syncFrom, alternateFile);
      // Request data file moves.
      mover.addMoveRequest(differenceFile, new File(syncToDir,
        CURRENT_DATA_FILE + oldVersion), "syncFile difference 526");
      mover.addMoveRequest(alternateFile, currentData,
        "syncFile data 528");
    }
    catch (IOException e)
    {
      ioe = e;
    }
    finally
    {
      IOException e = (IOException)closer.close();
      if (ioe == null)
      {
	ioe = e;
      }
      if (ioe != null)
      {
	System.err.println(ioe);
	ioe.printStackTrace();
	throw new DirSyncInternalException("IO error syncing file",
	  ioe);
      }
    }
  }

  /** Interpret a version file. Return whether the object should
   *  exist at the given version. Delete target if not.
   *  In laterVersions we want to return the numbers of difference
   *  files to construct the target version from the current version.
   *  A difference file with version # x is created when we replace
   *  version x with an existing file of some version > x
   *  Version x may or may not exist itself. We want
   *  that difference file if our target version is <= x.
   */
  private boolean handleVersions(File syncDir, long version,
    File target, List laterVersions) throws IOException,
    DirSyncInternalException
  {
    Closer closer = new Closer();
    boolean shouldExist = false;
    File versionFile = new File(syncDir, CURRENT_VERSION_FILE);
    if (!versionFile.exists())
    {
      throw new DirSyncInternalException(
	"No version file in extractFile");
    }
    try
    {
      BufferedReader br = new BufferedReader(
	new FileReader(versionFile));
      closer.addReader(br, versionFile.getPath());
      if (!DATA_VERSION_HEADER.equals(br.readLine()))
      {
	throw new DirSyncInternalException(
	  "Did not recognise version file header");
      }
      // Version number of last unused version seen
      long lastNumber = 0;
      // Whether there is any last unused existing version
      boolean lastValid = false;
      long lastVersion = FIRST_VERSION_NUMBER - 1;
      for (;;)
      {
	String line = br.readLine();
	if (line == null)
	{
	  break;
	}
	int index = line.indexOf(AFTER_VERSION_NUMBER);
	if (index < 0)
	{
	  throw new DirSyncInternalException(
	    "Could not find prefix in version file");
	}
	long versionHere = -1;
	try
	{
	  versionHere =
	    (new Long(line.substring(0, index).trim())).longValue();
	}
	catch (NumberFormatException nfe)
	{
	  throw new DirSyncInternalException(
	    "Could not read version number in version file");
	}
	if (versionHere <= lastVersion)
	{
	  throw new DirSyncInternalException(
	    "version numbers running backwards");
	}
	lastVersion = versionHere;
	boolean exists;
	if (line.length() < (index + 2))
	{
	  throw new DirSyncInternalException(
	    "Can be No existence info after version string");
	}
	String existence = line.substring(index +
	  AFTER_VERSION_NUMBER.length());
	if (VERSION_EXISTS.equals(existence))
	{
	  exists = true;
	}
	else if (VERSION_DELETED.equals(existence))
	{
	  exists = false;
	}
	else
	{
	  throw new DirSyncInternalException(
	    "Bad existence info in version file");
	}
	if (versionHere <= version)
	{
	  // whether file should exist or not depends on the
	  // status at the last applicable alteration
	  shouldExist = exists;
	}
	if (lastValid && exists && (version < versionHere))
	{
	  // If e.g. we want the most recent version of the file,
	  // there are no relevant difference files at all. So we
	  // don't want to consider any version number for addition
	  // until we know it isn't the very last version number
	  // recorded. Therefore here we perhaps add not the version
	  // number we have just seen, but the version number seen
	  // before that, if any.
	  // Without the lastNumber difference file we can create
	  // info valid for all versions >= versionHere, so we need it
	  // iff we are looking for something earlier than that.
	  laterVersions.add(new Long(lastNumber));
	}
	lastNumber = versionHere;
	lastValid = true;
      }
      if (!shouldExist)
      {
	/*
	No point trying to delete here because we refuse to restore
	except to empty directories. Should not delete existing
	stuff here because a directory may be replaced by a file
	or vice versa, and we treat that as the deletion of the former
	and the creation of the latter. We don't want to try and delete
	a directory because a file of the same name no longer exists.

	target.delete();
	if (target.exists())
	{
	  throw new DirSyncInternalException("Could not delete target");
	}
	*/
	return false;
      }
      return true;
    }
    finally
    {
      IOException ioe = (IOException)closer.close();
      if (ioe != null)
      {
        throw ioe;
      }
    }
  }

  /** Simple routine to list files, returning zero length list
   *  instead of null
   */
  private static File[] nonNullListFiles(File f)
  {
    if (!f.isDirectory())
    {
      System.err.println("Funny trying to list " + f.getPath());
    }
    File[] list = f.listFiles();
    if (list != null)
    {
      return list;
    }
    return new File[0];
  }

  /** Extract a single file, returning true if exists */
  public boolean extractFile(File syncDir, long version, File target)
    throws DirSyncInternalException, DirSyncUserException
  {
    if (!syncDir.getName().startsWith(FILE_PREFIX))
    {
      throw new DirSyncUserException(
        "Sync directory does not start with file prefix");
    }
    List versionsPassed = new ArrayList();
    IOException ioe = null;
    try
    {
      boolean shouldExist = handleVersions(syncDir, version, target, 
        versionsPassed);

      if (!shouldExist)
      {
        return false;
      }
      // Want this check after if (!shouldExist) because we might
      // have already restored a directory here - OK if no file
      // targeted here
      if (target.exists())
      {
	throw new DirSyncUserException("File target " + target +
	  "exists");
      }
      // here to retrieve info
      File sourceFile = new File(syncDir, CURRENT_DATA_FILE);
      // whether the current file is a working file to be deleted
      boolean disposableSource = false;
      // Start off with source file, which is the last version seen,
      // and apply earlier versions
      File directory = target.getParentFile();
      if (directory == null)
      {
        throw new DirSyncInternalException(
	  "Target file has null parent");
      }
      for (int i = versionsPassed.size() - 1; i >= 0; i--)
      {
        File newFile = File.createTempFile("temp", "tmp", directory);
	File control = new File(syncDir, CURRENT_DATA_FILE +
	  versionsPassed.get(i).toString());
	// System.err.println("Applying control file " + control);
	FileTransformBuilder.applyTransform(control,
	  sourceFile, newFile);
	if (disposableSource)
	{
	  if (!sourceFile.delete())
	  {
	    throw new DirSyncInternalException(
	      "Could not delete temporary file " + sourceFile.getPath());
	  }
	}
	sourceFile = newFile;
	disposableSource = true;
      }
      if (disposableSource && sourceFile.renameTo(target))
      {
        return true;
      }
      copyFile(sourceFile, target);
      if (disposableSource)
      {
        if (!sourceFile.delete())
	{
	  throw new DirSyncInternalException(
	    "Could not delete temporary file " + sourceFile.getPath());
	}
      }
      return true;
    }
    catch (IOException e)
    {
      System.err.println("Exception " + e);
      e.printStackTrace();
      throw new DirSyncInternalException("IO Exception");
    }
  }

  /** Extract a directory */
  public void extractDirectory(File syncDir, long version, File target)
    throws DirSyncInternalException, DirSyncUserException
  {
    // This is filled in, but we don't need it because we don't
    // use differences between directories: we just need to know
    // if it exists at a given version number or not.
    List versionsPassed = new ArrayList();
    IOException ioe = null;
    if (!syncDir.getName().startsWith(DIR_PREFIX))
    {
      throw new DirSyncUserException(
        "Sync directory does not start with directory prefix");
    }
    try
    {
      boolean shouldExist = handleVersions(syncDir, version, target, 
        versionsPassed);

      if (!shouldExist)
      {
        return;
      }
    }
    catch (IOException e)
    {
      throw new DirSyncInternalException("IO Exception");
    }
    if (target.exists())
    {
      throw new DirSyncUserException("Directory target " + target +
        " exists");
    }
    File[] contents = nonNullListFiles(syncDir);
    boolean exists = target.exists();
    if (!exists)
    {
      if (!target.mkdir())
      {
        throw new DirSyncInternalException("Could not create target");
      }
    }
    for (int i = 0; i < contents.length; i++)
    {
      File source = contents[i];
      if (!source.isDirectory())
      { // some sort of control file
        continue;
      }
      String name = source.getName();
      if (name.startsWith(DIR_PREFIX))
      {
	extractDirectory(source, version, 
	  new File(target, name.substring(FILE_PREFIX.length())));
      }
      else if (name.startsWith(FILE_PREFIX))
      {
	extractFile(source, version,
	    new File(target, name.substring(DIR_PREFIX.length())));
      }
      else
      {
        throw new DirSyncInternalException(
	  "Unknown directory " + source.getPath() + " found");
      }
    }
  }

  /** Synchronize a directory. Treat non-directory targets as if they
   *  did not exist because they will be saved off separately as files.
   *  Sub-files in the directory are not ignored, of course, but
   *  handled by calls to file sync routines.
   */
  private void syncDirectory(File syncFromDir, File syncToDir,
    long version, FileMover mover, boolean verbose)
    throws DirSyncInternalException, DirSyncUserException
  {
    if (!syncToDir.getName().startsWith(DIR_PREFIX))
    {
      throw new DirSyncUserException(
        "Directory sync directory does not start with " + DIR_PREFIX);
    }
    File alternateVersionFile = new File(syncToDir,
      ALTERNATE_VERSION_FILE);
    boolean currentExists = syncFromDir.exists() && 
      syncFromDir.isDirectory();
    File currentVersionFile =
      new File(syncToDir, CURRENT_VERSION_FILE);
    if (!syncToDir.exists())
    { // No previous data for this directory
      if (!currentExists)
      {
	return;
      }
      // create new directory
      if (!syncToDir.mkdir())
      {
	throw new DirSyncInternalException(
	  "Could not create directory " + syncToDir);
      }
      addVersion(currentVersionFile, alternateVersionFile,
	version, currentExists, mover);
      // Handle files in directory
      File[] files = nonNullListFiles(syncFromDir);
      for (int i = 0; i < files.length; i++)
      {
	File sourceFile = files[i];
	boolean isDirectory = sourceFile.isDirectory();
	boolean isFile = sourceFile.isFile();
	if (isDirectory == isFile)
	{ // both a file and a directory ?!?!?!
	  throw new DirSyncInternalException(
	    "Cannot work with source file " + sourceFile);
	}
	if (isDirectory)
	{
	  File newDirectory = new File(syncToDir,
	    DIR_PREFIX + sourceFile.getName());
	  syncDirectory(sourceFile, newDirectory, version, mover,
	    verbose);
	}
	else
	{
	  File newFileDirectory = new File(syncToDir,
	    FILE_PREFIX + sourceFile.getName());
	  syncFile(sourceFile, newFileDirectory, version, mover,
	           verbose);
	}
      }
      return;
    }
    // current directory exists
    if (!currentVersionFile.exists())
    {
      throw new DirSyncInternalException("Version file " +
	currentVersionFile + " does not exist");
    }
    addVersion(currentVersionFile, alternateVersionFile, version,
      currentExists, mover);
    if (!currentExists)
    {
      return;
    }
    // Handle files in directory
    File[] files = nonNullListFiles(syncFromDir);
    // Map from file name to boolean saying whether directory or not.
    // Must be map on files, not strings, because Windows can play
    // games with us by changing the case of the file name: this
    // is actually still the same file
    Map handled = new HashMap();
    for (int i = 0; i < files.length; i++)
    {
      File sourceFile = files[i];
      boolean isDirectory = sourceFile.isDirectory();
      boolean isFile = sourceFile.isFile();
      if (isDirectory == isFile)
      {
	throw new DirSyncInternalException(
	  "Cannot work with source file " + sourceFile);
      }
      String name = sourceFile.getName();
      if (isDirectory)
      {
	File newDirectory = new File(syncToDir, DIR_PREFIX + name);
	syncDirectory(sourceFile, newDirectory, version, mover, 
	  verbose);
      }
      else
      {
	File newFileDirectory = new File(syncToDir,
	  FILE_PREFIX + name);
	syncFile(sourceFile, newFileDirectory, version, mover, verbose);
      }
      handled.put(new File(syncFromDir, name),
        Boolean.valueOf(isDirectory));
    }
    // Check for files and directories that have now vanished
    files = nonNullListFiles(syncToDir);
    for (int i = 0; i < files.length; i++)
    {
      File syncFile = files[i];
      String fullName = syncFile.getName();
      String fileName;
      boolean nowDirectory;
      if (!syncFile.isDirectory())
      {
	// must be some sort of control file
	continue;
      }
      if (fullName.startsWith(FILE_PREFIX))
      {
	nowDirectory = false;
	fileName = fullName.substring(FILE_PREFIX.length());
      }
      else if (fullName.startsWith(DIR_PREFIX))
      {
	nowDirectory = true;
	fileName = fullName.substring(DIR_PREFIX.length());
      }
      else
      { // ???
	throw new DirSyncInternalException(
	  "Did not expect directory " + syncFile);
      }
      File newSource = new File(syncFromDir, fileName);
      Boolean oldType = (Boolean)handled.get(newSource);
      if ((oldType != null) &&
	(oldType.booleanValue() == nowDirectory))
      { // already handled this
	continue;
      }
      if (nowDirectory)
      {
	syncDirectory(newSource, syncFile, version, mover, verbose);
      }
      else
      {
	syncFile(newSource, syncFile, version, mover, verbose);
      }
    }
  }

  /** Run from user command to synchronize a file or directory against
   *  the target location
   */
  public long syncWith(File syncFrom, File syncToDir, boolean isFile,
    boolean isDirectory, boolean verbose)
    throws DirSyncUserException, DirSyncInternalException
  {
    long version = -1;
    File locationCheck = syncToDir;
    // check that target location makes sense
    for (;;)
    {
      if (locationCheck.equals(topLevel))
      {
        break;
      }
      locationCheck = locationCheck.getParentFile();
      if (locationCheck == null)
      {
        throw new DirSyncUserException(
	  "syncToDir " + syncToDir + " is not below top level of sync directory");
      }
    }
    // check what we are going to synchronize with
    Closer closer = new Closer();
    // Use this in conjunction with file rewriting to not quite
    // get atomicity
    FileMover mover = new FileMover();
    try
    {
      // Work out new version number and create new copy of version file
      BufferedReader br;
      try
      {
	// To do this, read the top-level version file
	br = new BufferedReader(new FileReader(versionFile));
      }
      catch (FileNotFoundException fnf)
      {
        throw new DirSyncInternalException("Could not find version file");
      }
      closer.addReader(br, versionFile.getPath());
      File alternateVersion = new File(topLevel,
        ALTERNATE_TOP_VERSION_FILE);
      PrintWriter pw = new PrintWriter(new BufferedWriter(
        new FileWriter(alternateVersion)));
      closer.addWriter(pw, alternateVersion.getPath());
      long lastVersion = FIRST_VERSION_NUMBER - 1;
      version = FIRST_VERSION_NUMBER;
      String line = "";
      if (!TOP_VERSION_HEADER.equals(br.readLine()))
      {
        throw new DirSyncInternalException(
	  "Did not recognise version header");
      }
      pw.println(TOP_VERSION_HEADER);
      try
      {
	for (;;)
	{
	  line = br.readLine();
	  if (line == null)
	  {
	    break;
	  }
	  int colon = line.indexOf(AFTER_VERSION_NUMBER);
	  if (colon < 0)
	  {
	    throw new DirSyncInternalException(
	      "Bad line in version file: " + line);
	  }
	  version = (new Long(line.substring(0, colon).trim()))
	    .longValue();
	  if (version <= lastVersion)
	  {
	    throw new DirSyncInternalException(
	      "Top level version numbers going backwards");
	  }
	  lastVersion = version;
	  pw.println(line);
	}
	version++;
        pw.println(getVersionString(version));
      }
      catch (NumberFormatException nfe)
      {
	throw new DirSyncInternalException("Bad version number in version file: " + line);
      }
      IOException ie = (IOException)closer.close();
      if (ie != null)
      {
        throw ie;
      }
      if (pw.checkError())
      {
        throw new DirSyncInternalException(
	  "Could not update top-level version file");
      }
      // Got the version number
      if (isFile)
      {
        syncFile(syncFrom, syncToDir, version, mover, verbose);
      }
      else if (isDirectory)
      {
        syncDirectory(syncFrom, syncToDir, version, mover, verbose);
      }
      // Add request for move of version file last of all so it
      // succeeds only if everything else does
      mover.addMoveRequest(alternateVersion, versionFile,
        "syncWith version move");
      // Ensure all File IO is completed before moves
      closer.close();
      if (!mover.run())
      {
        throw new DirSyncInternalException(
	  "Concluding file moves failed");
      }
    }
    catch (IOException ioe)
    {
      throw new DirSyncInternalException("IO error", ioe);
    }
    finally
    {
      closer.close();
    }
    return version;
  }

  /** Main program acts as CLI. Commands are <br>
   *  quiet turns off verbose mode and can be used as prefix
   *  xf &lt;sync dir> &lt;version number> &lt;target file><br>
   *  xd &lt;sync dir> &lt;version number> &lt;target directory><br>
   *  syncFile &lt;source file> &lt;sync dir><br>
   *  syncDir &lt;source dir> &lt;sync dir><br>
   *  create &lt;sync dir> Creates a top-level directory. The 
   *  directory must start with d.
   */
  public static void main(String[] s) throws DirSyncInternalException,
    DirSyncUserException
  {
    boolean trouble = false;
    boolean verbose = true;
    if ((s.length > 0) && "quiet".equals(s[0]))
    {
      String[] t = new String[s.length - 1];
      System.arraycopy(s, 1, t, 0, t.length);
      s = t;
      verbose = false;
    }
    if (s.length == 4)
    {
      File syncDir = new File(s[1]);
      DirSync ds = new DirSync(syncDir, true);
      long version = -1;
      try
      {
        Long ll = new Long(s[2].trim());
	version = ll.longValue();
      }
      catch (NumberFormatException nfe)
      {
        System.err.println("Could not read version in " + s[2]);
	trouble = true;
      }
      if (!trouble)
      {
	if ("xf".equals(s[0]))
	{
	  ds.extractFile(syncDir, version, new File(s[3]));
	}
	else if ("xd".equals(s[0]))
	{
	  ds.extractDirectory(syncDir, version, new File(s[3]));
	}
	else
	{
	  trouble = true;
	}
      }
    }
    else if (s.length == 3)
    {
      File syncDir = new File(s[2]);
      DirSync ds = new DirSync(syncDir, true);
      File source = new File(s[1]);
      if ("syncFile".equals(s[0]))
      {
	ds.syncWith(source, syncDir, true,
	  false, verbose);
      }
      else if ("syncDir".equals(s[0]))
      {
	ds.syncWith(source, syncDir, false,
	  true, verbose);
      }
      else
      {
	trouble = true;
      }
    }
    else if (s.length == 2)
    {
      if ("clean".equals(s[0]))
      {
	File syncDir = new File(s[1]);
	DirSync.clean(syncDir, verbose);
      }
      else if ("create".equals(s[0]))
      {
        DirSync.createTopLevel(new File(s[1]));
      }
      else
      {
        trouble = true;
      }
    }
    else
    {
      trouble = true;
    }
    if (trouble)
    {
      System.out.println("Args are xf <fileDir> <version> <target> " +
        "or xd <dirDir> <version> <target> or " +
	"syncFile <source> <syncDir> or syncDir <source> <syncDir> " +
	"or clean <syncDir> or " +
	"or create <syncDir - must start with d> or " +
	"or above prefixed by quiet ");
    }
  }
}
