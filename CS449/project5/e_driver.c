
#include <linux/fs.h>
#include <linux/init.h>
#include <linux/miscdevice.h>
#include <linux/module.h>

#include <asm/uaccess.h>
#include "e.h"

/*
 * Function e_driver to call read().
 * It reads in the correct count of e when
 * the buffer is passed into the call
 */

static ssize_t e_read(struct file * file, char * buf, 
			  size_t count, loff_t *ppos)
{
	int len;
	/*
 	* Since we only want a specific window of e
 	* ppos to ppos + count 
 	*/
	e(buf, count);

	len = strlen(buf); /* Don't include the null byte. */

	if (count < len)
		return -EINVAL;

	/*
	 * If file position is non-zero, then assume the string has
	 * been read and indicate there is no more data to be read.
	 */
	if (*ppos != 0)
		return 0;
	/*
	 * Tell the user how much data we wrote.
	 */
	*ppos = len;

	return len;
}

/*
 * The only file operation we care about is read.
 */

static const struct file_operations e_fops = {
	.owner		= THIS_MODULE,
	.read		= e_read,
};

static struct miscdevice e_dev = {
	/*
	 * We don't care what minor number we end up with, so tell the
	 * kernel to just pick one.
	 */
	MISC_DYNAMIC_MINOR,
	/*
	 * Name ourselves /dev/e_driver.
	 */
	"e_driver",
	/*
	 * What functions to call when a program performs file
	 * operations on the device.
	 */
	&e_fops
};

static int __init
e_init(void)
{
	int ret;

	/*
	 * Create the "e" device.
	 * Udev will automatically create the /dev/e device using
	 * the default rules.
	 */
	ret = misc_register(&e_dev);
	if (ret)
		printk(KERN_ERR
		       "Unable to register \"e digits\" misc device\n");

	return ret;
}

module_init(e_init);

static void __exit
e_exit(void)
{
	misc_deregister(&e_dev);
}

module_exit(e_exit);

MODULE_LICENSE("GPL");
MODULE_AUTHOR("Jamie Min <jam433@pitt.edu>");
MODULE_DESCRIPTION("\"e digits\" minimal module");
MODULE_VERSION("dev");
