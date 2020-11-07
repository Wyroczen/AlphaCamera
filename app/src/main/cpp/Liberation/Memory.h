#include "liberation.h"

namespace Memory
{
	inline bool Protect(void *addr, size_t length)
	{
		size_t pagesize = sysconf(_SC_PAGESIZE);
		uintptr_t start = (uintptr_t) addr;
		uintptr_t end = start + length;
		uintptr_t pagestart = start & -pagesize;
		return mprotect((void*)pagestart, end - pagestart, PROT_READ|PROT_EXEC) < 0;
	}

	inline bool UnProtect(void *addr, size_t length)
	{
		size_t pagesize = sysconf(_SC_PAGESIZE);
		uintptr_t start = (uintptr_t) addr;
		uintptr_t end = start + length;
		uintptr_t pagestart = start & -pagesize;
		return mprotect((void*)pagestart, end - pagestart, PROT_READ|PROT_WRITE|PROT_EXEC) < 0;
	}

	template<typename T1, typename T2> inline void Write(T1 memaddr, T2 bytes, size_t length)
	{
		if (UnProtect((void*) memaddr, length)) {
            __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Returning after UnProtect!");
			return;
		}
        __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Writing to memory!");

        __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Memory Write memaddr/dest (hex): %zx\n", (size_t) memaddr); //as hex
        __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Memory Write bytes (hex): %zx\n", (size_t) bytes); //as hex
        __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Memory Write length (hex): %zd\n", length); //as signed decimal

		//memcpy((void*) memaddr, (void*) bytes, length); //original
		size_t num = 3; //my code to force insert 3 because I want to replace 5 from lib
		__android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Memory Write bytes NUM addr (hex): %zx\n", (size_t) &num); //as hex
		memcpy((void*) memaddr, (void*) &num, sizeof(num));

		Protect((void*) memaddr, length);
	}	

	template<typename T1, typename T2> inline void Read(T1 memaddr, T2 dest, size_t length)
	{
		__android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Memory Read memaddr (hex): %zx\n", (size_t) memaddr); //as hex
		__android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Memory Read dest (hex): %zx\n", (size_t) dest); //as hex
		__android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Memory Read length (hex): %zd\n", length); //as signed decimal

		memcpy((void*) dest, (void*) memaddr, length);
	}
}